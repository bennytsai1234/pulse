package com.pulse.music.player.service

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
class PulseAudioService : MediaLibraryService() {

    // 透過 Hilt 注入已經配置好的 ExoPlayer 實例
    // 我們會在 DI Module 中設定 Gapless 播放與 Audio Focus
    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var userPreferencesRepository: com.pulse.music.domain.repository.UserPreferencesRepository

    @Inject
    lateinit var musicRepository: com.pulse.music.domain.repository.MusicRepository

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
                .add(androidx.media3.session.SessionCommand(com.pulse.music.core.common.PlayerConstants.ACTION_SET_SLEEP_TIMER, Bundle.EMPTY))
                .add(androidx.media3.session.SessionCommand(com.pulse.music.core.common.PlayerConstants.ACTION_CANCEL_SLEEP_TIMER, Bundle.EMPTY))
                .add(androidx.media3.session.SessionCommand(com.pulse.music.core.common.PlayerConstants.ACTION_GET_AUDIO_SESSION_ID, Bundle.EMPTY))
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
                com.pulse.music.core.common.PlayerConstants.ACTION_SET_SLEEP_TIMER -> {
                    val minutes = args.getInt(com.pulse.music.core.common.PlayerConstants.EXTRA_SLEEP_TIMER_MINUTES)
                    if (minutes > 0) {
                        startSleepTimer(minutes)
                    }
                    return com.google.common.util.concurrent.Futures.immediateFuture(
                        androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_SUCCESS)
                    )
                }
                com.pulse.music.core.common.PlayerConstants.ACTION_GET_AUDIO_SESSION_ID -> {
                    val resultArgs = Bundle().apply {
                        putInt(com.pulse.music.core.common.PlayerConstants.EXTRA_AUDIO_SESSION_ID, player.audioSessionId)
                    }
                    return com.google.common.util.concurrent.Futures.immediateFuture(
                        androidx.media3.session.SessionResult(androidx.media3.session.SessionResult.RESULT_SUCCESS, resultArgs)
                    )
                }
                com.pulse.music.core.common.PlayerConstants.ACTION_CANCEL_SLEEP_TIMER -> {
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
                        .setTitle(getString(com.pulse.music.player.R.string.browse_root_title))
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
            return com.google.common.util.concurrent.Futures.submit(
                java.util.concurrent.Callable {
                    val children = when (parentId) {
                        MEDIA_ROOT_ID -> {
                            // Top-level categories for Android Auto
                            listOf(
                                buildBrowsableMediaItem(MEDIA_RECENT_ID, getString(com.pulse.music.player.R.string.browse_recent), androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS),
                                buildBrowsableMediaItem(MEDIA_ALL_SONGS_ID, getString(com.pulse.music.player.R.string.browse_all_songs), androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS),
                                buildBrowsableMediaItem(MEDIA_ALBUMS_ID, getString(com.pulse.music.player.R.string.browse_albums), androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS),
                                buildBrowsableMediaItem(MEDIA_ARTISTS_ID, getString(com.pulse.music.player.R.string.browse_artists), androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS)
                            )
                        }
                        MEDIA_RECENT_ID -> {
                            try {
                                val recentSongs = kotlinx.coroutines.runBlocking { musicRepository.getRecentlyAdded().first() }
                                recentSongs.map { it.toMediaItem() }
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }
                        MEDIA_ALL_SONGS_ID -> {
                            try {
                                val allSongs = kotlinx.coroutines.runBlocking { musicRepository.getSongs().first() }
                                allSongs.map { it.toMediaItem() }
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }
                        MEDIA_ALBUMS_ID -> {
                             try {
                                val albums = kotlinx.coroutines.runBlocking { musicRepository.getAlbums().first() }
                                albums.map { album ->
                                    androidx.media3.common.MediaItem.Builder()
                                        .setMediaId("album/${album.id}")
                                        .setMediaMetadata(
                                            androidx.media3.common.MediaMetadata.Builder()
                                                .setIsBrowsable(true)
                                                .setIsPlayable(false)
                                                .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_ALBUM)
                                                .setTitle(album.title)
                                                .setArtist(album.artist)
                                                .build()
                                        )
                                        .build()
                                }
                             } catch (e: Exception) {
                                 emptyList()
                             }
                        }
                        MEDIA_ARTISTS_ID -> {
                            try {
                                val artists = kotlinx.coroutines.runBlocking { musicRepository.getArtists().first() }
                                artists.map { artist ->
                                    androidx.media3.common.MediaItem.Builder()
                                        .setMediaId("artist/${artist.name}")
                                        .setMediaMetadata(
                                            androidx.media3.common.MediaMetadata.Builder()
                                                .setIsBrowsable(true)
                                                .setIsPlayable(false)
                                                .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_ARTIST)
                                                .setTitle(artist.name)
                                                .setSubtitle("${artist.songCount} songs")
                                                .build()
                                        )
                                        .build()
                                }
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }
                        else -> {
                           // Handle sub-categories (Album content, Artist content)
                           if (parentId.startsWith("album/")) {
                               val albumId = parentId.removePrefix("album/").toLongOrNull() ?: -1L
                               if (albumId != -1L) {
                                   try {
                                       val songs = kotlinx.coroutines.runBlocking { musicRepository.getSongsByAlbumId(albumId).first() }
                                       songs.map { it.toMediaItem() }
                                   } catch (e: Exception) { emptyList() }
                               } else emptyList()
                           } else if (parentId.startsWith("artist/")) {
                               // Note: Repository currently lacks getSongsByArtist, just filtering all songs for MVP
                               // Efficient implementation should add getSongsByArtist to Repository later.
                               try {
                                   val artistName = parentId.removePrefix("artist/")
                                   val allSongs = kotlinx.coroutines.runBlocking { musicRepository.getSongs().first() }
                                   allSongs.filter { it.artist == artistName }.map { it.toMediaItem() }
                               } catch (e: Exception) { emptyList() }
                           }
                           else {
                               emptyList()
                           }
                        }
                    }
                    LibraryResult.ofItemList(children, params)
                },
                com.google.common.util.concurrent.MoreExecutors.directExecutor()
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

    // Helper function moved from MusicServiceConnection for reusability if needed,
    // but here we duplicate/implement locally to ensure Service independence or import it.
    // For this context, we will perform simple mapping or reuse if accessible.
    // Ideally, mapper should be in a shared utility or extension.
    // Since MusicServiceConnection.kt is in 'manager' and this is 'service', let's implement a local helper
    // or rely on the one defined at the bottom of THIS file if present?
    // Checking file content... no toMediaItem extension in THIS file currently based on view_file output.
    // Wait, I see toSong/toMediaItem at the bottom of MusicServiceConnection.kt.
    // Let's add the extension here to avoid circular dependency or visibility issues.

    private fun com.pulse.music.domain.model.Song.toMediaItem(): androidx.media3.common.MediaItem {
        val extras = Bundle().apply {
            putString("DATA_PATH", dataPath)
            putLong("ALBUM_ID", albumId)
        }
        val metadata = androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setExtras(extras)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .build()

        return androidx.media3.common.MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(contentUri)
            .setMediaMetadata(metadata)
            .build()
    }

    companion object {
        const val MEDIA_ROOT_ID = "PULSE_root"
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

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, librarySessionCallback)
            .setSessionActivity(openActivityIntent)
            .build()

        restorePlaybackState()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateWidget()
                if (!isPlaying) {
                     savePlaybackState()
                }
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                updateWidget()
                savePlaybackState()
            }

            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                 if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                     savePlaybackState()
                 }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("PulseAudioService", "Player Error: ${error.message}")
                android.widget.Toast.makeText(applicationContext, "Playback Error: ${error.errorCodeName}", android.widget.Toast.LENGTH_SHORT).show()

                // Attempt to skip to next track if available
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                    player.prepare()
                    player.play()
                }
            }
        })
    }

    private fun updateWidget() {
        val intent = Intent("com.pulse.music.action.UPDATE_WIDGET")
        val currentMediaItem = player.currentMediaItem
        val metadata = currentMediaItem?.mediaMetadata

        intent.putExtra("com.pulse.music.extra.IS_PLAYING", player.isPlaying)
        intent.putExtra("com.pulse.music.extra.TITLE", metadata?.title?.toString())
        intent.putExtra("com.pulse.music.extra.ARTIST", metadata?.artist?.toString())

        // We can't access WidgetConstants directly if it's in core/common and player depends on it.
        // Assuming player depends on core/common (it should).
        // If not, we use string literals to match WidgetConstants.

        sendBroadcast(intent)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        savePlaybackState()
        // 當使用者從「最近任務」將 App 滑掉時，確保播放器如果沒在播放就停止 Service
        val player = mediaLibrarySession?.player
        if (player == null || !player.playWhenReady || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    private fun restorePlaybackState() {
        serviceScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val queueIds = userPreferencesRepository.lastQueueMediaIds.first()
            val lastIndex = userPreferencesRepository.lastQueueIndex.first()
            val lastPosition = userPreferencesRepository.lastPlayedPosition.first()

            if (queueIds.isNotEmpty()) {
                val allSongs = musicRepository.getSongs().first()
                val songMap = allSongs.associateBy { it.id.toString() }
                val queueItems = queueIds.mapNotNull { id -> songMap[id]?.toMediaItem() }

                if (queueItems.isNotEmpty()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        player.setMediaItems(queueItems, lastIndex, lastPosition)
                        player.prepare()
                        player.pause()
                    }
                }
            }
        }
    }

    private fun savePlaybackState() {
        val currentIndex = player.currentMediaItemIndex
        val currentPosition = player.currentPosition
        val currentMediaId = player.currentMediaItem?.mediaId

        if (player.mediaItemCount == 0) return

        val queueIds = mutableListOf<String>()
        for (i in 0 until player.mediaItemCount) {
            player.getMediaItemAt(i).mediaId.let { queueIds.add(it) }
        }

        serviceScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            userPreferencesRepository.setLastQueueMediaIds(queueIds)
            userPreferencesRepository.setLastQueueIndex(currentIndex)
            userPreferencesRepository.setLastPlayedPosition(currentPosition)
            if (currentMediaId != null) {
                userPreferencesRepository.setLastPlayedMediaId(currentMediaId)
            }
        }
    }

    override fun onDestroy() {
        savePlaybackState()
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }
}
