package com.gemini.music.ui.nowplaying

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.gemini.music.domain.model.LyricLine
import com.gemini.music.domain.model.RepeatMode
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.usecase.CycleRepeatModeUseCase
import com.gemini.music.domain.usecase.FormattedPlaybackState
import com.gemini.music.domain.usecase.GetFormattedPlaybackStateUseCase
import com.gemini.music.domain.usecase.GetLyricsUseCase
import com.gemini.music.domain.usecase.GetMusicStateUseCase
import com.gemini.music.domain.usecase.PlayQueueItemUseCase
import com.gemini.music.domain.usecase.RemoveQueueItemUseCase
import com.gemini.music.domain.usecase.SeekToUseCase
import com.gemini.music.domain.usecase.SkipToNextUseCase
import com.gemini.music.domain.usecase.SkipToPreviousUseCase
import com.gemini.music.domain.usecase.TogglePlayPauseUseCase
import com.gemini.music.domain.usecase.ToggleShuffleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class NowPlayingUiState(
    val song: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val currentTime: String = "0:00",
    val totalTime: String = "0:00",
    val backgroundColor: Color = Color(0xFF1E1E1E),
    val onBackgroundColor: Color = Color.White,
    val lyrics: List<LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1,
    val waveform: List<Float> = emptyList(), // Normalized amplitudes
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    getMusicStateUseCase: GetMusicStateUseCase,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val getFormattedPlaybackStateUseCase: GetFormattedPlaybackStateUseCase,
    private val togglePlayPauseUseCase: TogglePlayPauseUseCase,
    private val seekToUseCase: SeekToUseCase,
    private val skipToNextUseCase: SkipToNextUseCase,
    private val skipToPreviousUseCase: SkipToPreviousUseCase,
    private val toggleShuffleUseCase: ToggleShuffleUseCase,
    private val cycleRepeatModeUseCase: CycleRepeatModeUseCase,
    private val playQueueItemUseCase: PlayQueueItemUseCase,
    private val removeQueueItemUseCase: RemoveQueueItemUseCase,
    private val getSongWaveformUseCase: com.gemini.music.domain.usecase.GetSongWaveformUseCase
) : ViewModel() {

    private val _paletteColor = MutableStateFlow(Pair(Color(0xFF1E1E1E), Color.White))

    private val musicState = getMusicStateUseCase()

    private val lyricsFlow = musicState.map { it.currentSong }
        .distinctUntilChanged()
        .flatMapLatest { song ->
            if (song != null) {
                flowOf(getLyricsUseCase(song.dataPath))
            } else {
                flowOf(emptyList())
            }
        }
    
    // Waveform Loading
    private val waveformFlow = musicState.map { it.currentSong }
        .distinctUntilChanged()
        .flatMapLatest { song ->
             if (song != null) {
                 kotlinx.coroutines.flow.flow {
                     val rawData = getSongWaveformUseCase(song.dataPath)
                     // Normalize to 0f..1f
                     val max = rawData.maxOrNull() ?: 1
                     val normalized = rawData.map { it.toFloat() / max }
                     emit(normalized)
                 }
             } else {
                 flowOf(emptyList())
             }
        }

    private val formattedPlaybackStateFlow: StateFlow<FormattedPlaybackState> = lyricsFlow.flatMapLatest { lyrics ->
        getFormattedPlaybackStateUseCase(lyrics)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FormattedPlaybackState())

    val uiState: StateFlow<NowPlayingUiState> = combine(
        musicState,
        formattedPlaybackStateFlow,
        lyricsFlow,
        waveformFlow,
        _paletteColor
    ) { state, formattedState, lyrics, waveform, palette ->
        NowPlayingUiState(
            song = state.currentSong,
            isPlaying = state.isPlaying,
            shuffleModeEnabled = state.shuffleModeEnabled,
            repeatMode = state.repeatMode,
            queue = state.queue,
            progress = formattedState.progress,
            currentTime = formattedState.currentTime,
            totalTime = formattedState.totalTime,
            currentLyricIndex = formattedState.currentLyricIndex,
            lyrics = lyrics,
            waveform = waveform,
            backgroundColor = palette.first,
            onBackgroundColor = palette.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = NowPlayingUiState()
    )


    fun onEvent(event: NowPlayingEvent) {
        when (event) {
            is NowPlayingEvent.PlayPauseToggle -> togglePlayPauseUseCase()
            is NowPlayingEvent.SeekTo -> seekToUseCase(event.position)
            is NowPlayingEvent.SkipNext -> skipToNextUseCase()
            is NowPlayingEvent.SkipPrevious -> skipToPreviousUseCase()
            is NowPlayingEvent.ToggleShuffle -> toggleShuffleUseCase()
            is NowPlayingEvent.ToggleRepeat -> cycleRepeatModeUseCase()
            is NowPlayingEvent.UpdatePalette -> extractColors(event.bitmap)
            is NowPlayingEvent.PlayQueueItem -> playQueueItemUseCase(event.index)
            is NowPlayingEvent.RemoveFromQueue -> removeQueueItemUseCase(event.index)
        }
    }

    private fun extractColors(bitmap: Bitmap?) {
        bitmap?.let { bmp ->
            Palette.from(bmp).generate { palette ->
                val vibrant = palette?.vibrantSwatch?.rgb ?:
                              palette?.darkVibrantSwatch?.rgb ?:
                              palette?.mutedSwatch?.rgb ?: 0xFF1E1E1E.toInt()
                _paletteColor.value = Pair(Color(vibrant), Color.White) // Assuming white is a safe "on" color
            }
        }
    }
}

sealed class NowPlayingEvent {
    data object PlayPauseToggle : NowPlayingEvent()
    data object SkipNext : NowPlayingEvent()
    data object SkipPrevious : NowPlayingEvent()
    data object ToggleShuffle : NowPlayingEvent()
    data object ToggleRepeat : NowPlayingEvent()
    data class SeekTo(val position: Float) : NowPlayingEvent()
    data class UpdatePalette(val bitmap: Bitmap?) : NowPlayingEvent()
    data class PlayQueueItem(val index: Int) : NowPlayingEvent()
    data class RemoveFromQueue(val index: Int) : NowPlayingEvent()
}
