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
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicController
import com.gemini.music.player.service.GeminiAudioService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext context: Context
) : MusicController {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var mediaController: MediaController? = null
    private val controllerFuture: ListenableFuture<MediaController>

    // 暴露給 UI 的狀態
    private val _musicState = MutableStateFlow(MusicState())
    override val musicState: StateFlow<MusicState> = _musicState.asStateFlow()

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
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSong(mediaItem)
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
