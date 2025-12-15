package com.gemini.music.player.manager

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.gemini.music.domain.model.MusicState
import com.gemini.music.domain.model.RepeatMode
import com.gemini.music.domain.model.ScrobbleEntry
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicController
import com.gemini.music.domain.repository.ScrobbleRepository
import com.gemini.music.player.service.GeminiAudioService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext context: Context,
    private val scrobbleRepository: ScrobbleRepository
) : MusicController {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var mediaController: MediaController? = null
    private val controllerFuture: ListenableFuture<MediaController>

    // 暴露給 UI 的狀態
    private val _musicState = MutableStateFlow(MusicState())
    override val musicState: StateFlow<MusicState> = _musicState.asStateFlow()
    
    // Scrobbling 追蹤
    private var currentPlayingSong: Song? = null
    private var playStartTime: Long = 0L
    private var accumulatedPlayTime: Long = 0L
    private var hasScrobbled: Boolean = false

    init {
        val sessionToken = SessionToken(context, ComponentName(context, GeminiAudioService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                setupPlayerListener()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _musicState.update { it.copy(isPlaying = isPlaying) }
                
                // 更新 Scrobble 追蹤
                if (isPlaying) {
                    // 開始播放，記錄開始時間
                    playStartTime = System.currentTimeMillis()
                } else {
                    // 暫停播放，累計播放時間
                    if (playStartTime > 0) {
                        accumulatedPlayTime += System.currentTimeMillis() - playStartTime
                        playStartTime = 0L
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // 先處理上一首歌的 Scrobble
                tryRecordScrobble()
                
                // 更新當前歌曲
                updateCurrentSong(mediaItem)
                
                // 重置 Scrobble 追蹤狀態
                if (mediaItem != null) {
                    currentPlayingSong = mediaItem.toSong()
                    playStartTime = if (mediaController?.isPlaying == true) System.currentTimeMillis() else 0L
                    accumulatedPlayTime = 0L
                    hasScrobbled = false
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _musicState.update { 
                     it.copy(isBuffering = playbackState == Player.STATE_BUFFERING) 
                }
            }
            
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _musicState.update { it.copy(shuffleModeEnabled = shuffleModeEnabled) }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _musicState.update { it.copy(repeatMode = RepeatMode.fromInt(repeatMode)) }
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                updateQueue()
            }
        })
        
        // 初始化狀態
        updateCurrentSong(mediaController?.currentMediaItem)
        updateQueue()
        _musicState.update { 
            it.copy(
                isPlaying = mediaController?.isPlaying == true,
                shuffleModeEnabled = mediaController?.shuffleModeEnabled == true,
                repeatMode = RepeatMode.fromInt(mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF)
            ) 
        }
    }

    private fun updateQueue() {
        val controller = mediaController ?: return
        val queue = mutableListOf<Song>()
        for (i in 0 until controller.mediaItemCount) {
            val item = controller.getMediaItemAt(i)
            queue.add(item.toSong())
        }
        _musicState.update { it.copy(queue = queue) }
    }

    private fun updateCurrentSong(mediaItem: MediaItem?) {
        if (mediaItem == null) return
        _musicState.update { it.copy(currentSong = mediaItem.toSong()) }
    }
    
    /**
     * 嘗試記錄 Scrobble。
     * 符合條件：播放超過 50% 或至少 4 分鐘。
     */
    private fun tryRecordScrobble() {
        val song = currentPlayingSong ?: return
        if (hasScrobbled) return
        
        // 計算總播放時間
        var totalPlayedTime = accumulatedPlayTime
        if (playStartTime > 0) {
            totalPlayedTime += System.currentTimeMillis() - playStartTime
        }
        
        // Scrobble 標準：播放超過 50% 或至少 4 分鐘（240 秒）
        val minDuration = (song.duration * 0.5).toLong()
        val scrobbleThreshold = minOf(minDuration, 240_000L).coerceAtLeast(30_000L)
        
        if (totalPlayedTime >= scrobbleThreshold) {
            hasScrobbled = true
            scope.launch {
                try {
                    val entry = ScrobbleEntry(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        duration = song.duration,
                        timestamp = System.currentTimeMillis()
                    )
                    scrobbleRepository.recordScrobble(entry)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun playSongs(songs: List<Song>, startIndex: Int) {
        val mediaItems = songs.map { it.toMediaItem() }
        mediaController?.run {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
        }
    }

    override fun removeSong(index: Int) {
        mediaController?.removeMediaItem(index)
    }
    
    override fun moveSong(from: Int, to: Int) {
        mediaController?.moveMediaItem(from, to)
    }
    
    override fun playSongAt(index: Int) {
        mediaController?.seekToDefaultPosition(index)
        mediaController?.play()
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun resume() {
        mediaController?.play()
    }

    override fun skipToNext() {
        mediaController?.seekToNext()
    }

    override fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    override fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }
    
    override fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }
    
    override fun cycleRepeatMode() {
        mediaController?.let {
            val nextMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            it.repeatMode = nextMode
        }
    }

    override fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }
    
    override fun getDuration(): Long {
        return mediaController?.duration ?: 0L
    }

    override fun setSleepTimer(minutes: Int) {
        val command = androidx.media3.session.SessionCommand(
            com.gemini.music.core.common.PlayerConstants.ACTION_SET_SLEEP_TIMER, 
            Bundle.EMPTY
        )
        val args = Bundle().apply {
            putInt(com.gemini.music.core.common.PlayerConstants.EXTRA_SLEEP_TIMER_MINUTES, minutes)
        }
        mediaController?.sendCustomCommand(command, args)
    }

    override fun cancelSleepTimer() {
        val command = androidx.media3.session.SessionCommand(
            com.gemini.music.core.common.PlayerConstants.ACTION_CANCEL_SLEEP_TIMER, 
            Bundle.EMPTY
        )
        mediaController?.sendCustomCommand(command, Bundle.EMPTY)
    }
}

fun MediaItem.toSong(): Song {
    val meta = mediaMetadata
    return Song(
        id = mediaId.toLongOrNull() ?: 0L,
        title = meta.title?.toString() ?: "Unknown",
        artist = meta.artist?.toString() ?: "Unknown",
        album = meta.albumTitle?.toString() ?: "Unknown",
        albumId = meta.extras?.getLong("ALBUM_ID") ?: 0L,
        duration = 0,
        contentUri = requestMetadata.mediaUri.toString(),
        dataPath = meta.extras?.getString("DATA_PATH") ?: ""
    )
}

fun Song.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putString("DATA_PATH", dataPath)
        putLong("ALBUM_ID", albumId)
    }

    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setExtras(extras)
        .build()
        
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(contentUri)
        .setMediaMetadata(metadata)
        .build()
}
