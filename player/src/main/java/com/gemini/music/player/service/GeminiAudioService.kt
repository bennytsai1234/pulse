package com.gemini.music.player.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject


/**
 * SIGMA Music 的核心播放服務。
 * 負責管理 ExoPlayer 生命週期、MediaSession 以及背景播放邏輯。
 */
@AndroidEntryPoint
class GeminiAudioService : MediaLibraryService() {

    // 透過 Hilt 注入已經配置好的 ExoPlayer 實例
    // 我們會在 DI Module 中設定 Gapless 播放與 Audio Focus
    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var userPreferencesRepository: com.gemini.music.domain.repository.UserPreferencesRepository

    private var mediaLibrarySession: MediaLibrarySession? = null

    private val serviceScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main)
    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    private val librarySessionCallback = object : MediaLibrarySession.Callback {
        // 這裡處理來自 UI (MediaController) 或 Android Auto/Wear OS 的指令
        // 例如：連接請求、瀏覽媒體庫等
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands = connectionResult.availableSessionCommands
                .buildUpon()
                .add(androidx.media3.session.SessionCommand(com.gemini.music.core.common.PlayerConstants.ACTION_SET_SLEEP_TIMER, Bundle.EMPTY))
                .add(androidx.media3.session.SessionCommand(com.gemini.music.core.common.PlayerConstants.ACTION_CANCEL_SLEEP_TIMER, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.accept(sessionCommands, connectionResult.availablePlayerCommands)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: androidx.media3.session.SessionCommand,
            args: Bundle
        ): com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.SessionResult> {
            when (customCommand.customAction) {
                com.gemini.music.core.common.PlayerConstants.ACTION_SET_SLEEP_TIMER -> {
                    val minutes = args.getInt(com.gemini.music.core.common.PlayerConstants.EXTRA_SLEEP_TIMER_MINUTES)
                    if (minutes > 0) {
                        startSleepTimer(minutes)
                    }
                    return com.google.common.util.concurrent.Futures.immediateFuture(
                        androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_SUCCESS)
                    )
                }
                com.gemini.music.core.common.PlayerConstants.ACTION_CANCEL_SLEEP_TIMER -> {
                    cancelSleepTimer()
                    return com.google.common.util.concurrent.Futures.immediateFuture(
                        androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_SUCCESS)
                    )
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
        
        // ==================== Android Auto 媒體瀏覽支援 ====================
        
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): com.google.common.util.concurrent.ListenableFuture<LibraryResult<androidx.media3.common.MediaItem>> {
            // Root item for Android Auto browsing
            val rootItem = androidx.media3.common.MediaItem.Builder()
                .setMediaId(MEDIA_ROOT_ID)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .setTitle("Gemini Music")
                        .build()
                )
                .build()
            return com.google.common.util.concurrent.Futures.immediateFuture(
                LibraryResult.ofItem(rootItem, params)
            )
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): com.google.common.util.concurrent.ListenableFuture<LibraryResult<com.google.common.collect.ImmutableList<androidx.media3.common.MediaItem>>> {
            val children = when (parentId) {
                MEDIA_ROOT_ID -> {
                    // Top-level categories for Android Auto
                    listOf(
                        buildBrowsableMediaItem(MEDIA_RECENT_ID, "Recently Played", androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS),
                        buildBrowsableMediaItem(MEDIA_ALL_SONGS_ID, "All Songs", androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS),
                        buildBrowsableMediaItem(MEDIA_ALBUMS_ID, "Albums", androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS),
                        buildBrowsableMediaItem(MEDIA_ARTISTS_ID, "Artists", androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS)
                    )
                }
                // TODO: Implement actual content loading from MusicRepository
                // For now, return empty for sub-categories until we inject repository
                else -> emptyList()
            }
            return com.google.common.util.concurrent.Futures.immediateFuture(
                LibraryResult.ofItemList(children, params)
            )
        }
        
        private fun buildBrowsableMediaItem(id: String, title: String, mediaType: Int): androidx.media3.common.MediaItem {
            return androidx.media3.common.MediaItem.Builder()
                .setMediaId(id)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setMediaType(mediaType)
                        .setTitle(title)
                        .build()
                )
                .build()
        }
    }
    
    companion object {
        const val MEDIA_ROOT_ID = "gemini_root"
        const val MEDIA_RECENT_ID = "recent"
        const val MEDIA_ALL_SONGS_ID = "all_songs"
        const val MEDIA_ALBUMS_ID = "albums"
        const val MEDIA_ARTISTS_ID = "artists"
    }

    private fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        sleepTimerJob = serviceScope.launch {
            val fadeEnabled = try { userPreferencesRepository.sleepTimerFadeOut.first() } catch (e: Exception) { false }
            val fadeDuration = try { userPreferencesRepository.sleepTimerFadeDuration.first() } catch (e: Exception) { 0 }
            
            // Total sleep time in millis
            val totalTime = minutes * 60 * 1000L
            val fadeDurationMs = fadeDuration * 1000L
            
            if (!fadeEnabled || fadeDuration <= 0 || totalTime <= fadeDurationMs) {
                 kotlinx.coroutines.delay(totalTime)
                 if (player.isPlaying) player.pause()
            } else {
                 // Wait until fade starts
                 kotlinx.coroutines.delay(totalTime - fadeDurationMs)
                 
                 // Start fade out
                 val steps = 20
                 val stepDuration = fadeDurationMs / steps
                 val initialVolume = player.volume
                 
                 for (i in 0..steps) {
                      val volume = initialVolume * (1f - i.toFloat() / steps)
                      player.volume = volume
                      kotlinx.coroutines.delay(stepDuration)
                 }
                 
                 if (player.isPlaying) player.pause()
                 player.volume = initialVolume // Restore volume
            }
            
            sleepTimerJob = null
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
    }

    override fun onCreate() {
        super.onCreate()
        
        // Listen to Playback Speed settings
        serviceScope.launch {
             userPreferencesRepository.playbackSpeed.collect { speed ->
                  player.setPlaybackSpeed(speed)
             }
        }
        // 建立 Activity PendingIntent，點擊通知欄時跳轉回 App
        val openActivityIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName) ?: Intent(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 初始化 MediaLibrarySession
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, librarySessionCallback)
            .setSessionActivity(openActivityIntent)
            .build()
            
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateWidget()
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                updateWidget()
            }
        })
    }

    private fun updateWidget() {
        val intent = Intent("com.gemini.music.action.UPDATE_WIDGET")
        val currentMediaItem = player.currentMediaItem
        val metadata = currentMediaItem?.mediaMetadata

        intent.putExtra("com.gemini.music.extra.IS_PLAYING", player.isPlaying)
        intent.putExtra("com.gemini.music.extra.TITLE", metadata?.title?.toString())
        intent.putExtra("com.gemini.music.extra.ARTIST", metadata?.artist?.toString())
        
        // We can't access WidgetConstants directly if it's in core/common and player depends on it.
        // Assuming player depends on core/common (it should).
        // If not, we use string literals to match WidgetConstants.
        
        sendBroadcast(intent)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // 當使用者從「最近任務」將 App 滑掉時，確保播放器如果沒在播放就停止 Service
        val player = mediaLibrarySession?.player
        if (player == null || !player.playWhenReady || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }
}
