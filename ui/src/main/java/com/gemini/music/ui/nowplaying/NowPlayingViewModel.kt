package com.gemini.music.ui.nowplaying

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.gemini.music.domain.model.LyricLine
import com.gemini.music.domain.model.Playlist
import com.gemini.music.domain.model.RepeatMode
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
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
    val gradientColors: List<Color> = listOf(Color(0xFF1E1E1E), Color.Black),
    val onBackgroundColor: Color = Color.White,
    val lyrics: List<LyricLine> = emptyList(),
    val currentLyricIndex: Int = -1,
    val waveform: List<Float> = emptyList(), // Normalized amplitudes
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val isFavorite: Boolean = false,
    val playlists: List<Playlist> = emptyList()
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
    private val getSongWaveformUseCase: com.gemini.music.domain.usecase.GetSongWaveformUseCase,
    private val toggleFavoriteUseCase: com.gemini.music.domain.usecase.favorites.ToggleFavoriteUseCase,
    private val isSongFavoriteUseCase: com.gemini.music.domain.usecase.favorites.IsSongFavoriteUseCase,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _paletteColors = MutableStateFlow(listOf(Color(0xFF1E1E1E), Color.Black))
    private val _onPaletteColor = MutableStateFlow(Color.White)

    private val musicState = getMusicStateUseCase()
    
    // Observe favorite state for the current song
    private val isFavoriteFlow = musicState.map { it.currentSong }
        .distinctUntilChanged()
        .flatMapLatest { song ->
            if (song != null) {
                isSongFavoriteUseCase(song.id)
            } else {
                flowOf(false)
            }
        }

    private val lyricsFlow = musicState.map { it.currentSong }
        .distinctUntilChanged()
        .flatMapLatest { song ->
            if (song != null) {
                // Fetch lyrics asynchronously
                kotlinx.coroutines.flow.flow {
                     emit(getLyricsUseCase(song))
                }
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
        _paletteColors,
        _onPaletteColor,
        combine(lyricsFlow, waveformFlow, isFavoriteFlow, musicRepository.getPlaylists()) { lyrics, waveform, isFavorite, playlists -> 
            object {
                val lyrics = lyrics
                val waveform = waveform
                val isFavorite = isFavorite
                val playlists = playlists
            }
        }
    ) { state, formattedState, palette, onPalette, supplemental ->
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
            lyrics = supplemental.lyrics,
            waveform = supplemental.waveform,
            isFavorite = supplemental.isFavorite,
            playlists = supplemental.playlists,
            backgroundColor = palette.firstOrNull() ?: Color(0xFF1E1E1E),
            gradientColors = palette,
            onBackgroundColor = onPalette
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
            is NowPlayingEvent.ToggleFavorite -> {
                val song = uiState.value.song
                if (song != null) {
                    viewModelScope.launch {
                        toggleFavoriteUseCase(song.id)
                    }
                }
            }
            is NowPlayingEvent.AddToPlaylist -> {
                val song = uiState.value.song
                if (song != null) {
                    viewModelScope.launch {
                        musicRepository.addSongToPlaylist(event.playlistId, song.id)
                    }
                }
            }
            is NowPlayingEvent.CreatePlaylistAndAdd -> {
                val song = uiState.value.song
                if (song != null) {
                    viewModelScope.launch {
                        val newId = musicRepository.createPlaylist(event.name)
                        musicRepository.addSongToPlaylist(newId, song.id)
                    }
                }
            }
        }
    }

    private fun extractColors(bitmap: Bitmap?) {
        bitmap?.let { bmp ->
            Palette.from(bmp).generate { palette ->
                val vibrant = palette?.vibrantSwatch?.rgb?.let { Color(it) } ?: Color(0xFF1E1E1E)
                val darkVibrant = palette?.darkVibrantSwatch?.rgb?.let { Color(it) } ?: Color.Black
                val muted = palette?.mutedSwatch?.rgb?.let { Color(it) } ?: Color.DarkGray
                
                // Construct a rich gradient: Vibrant -> Muted/Dark
                val gradient = listOf(vibrant, darkVibrant)
                
                _paletteColors.value = gradient
                
                // Calculate 'on' color
                val bodyTextColor = palette?.dominantSwatch?.bodyTextColor ?: android.graphics.Color.WHITE
                _onPaletteColor.value = Color(bodyTextColor)
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
    data object ToggleFavorite : NowPlayingEvent()
    data class SeekTo(val position: Float) : NowPlayingEvent()
    data class UpdatePalette(val bitmap: Bitmap?) : NowPlayingEvent()
    data class PlayQueueItem(val index: Int) : NowPlayingEvent()
    data class RemoveFromQueue(val index: Int) : NowPlayingEvent()
    data class AddToPlaylist(val playlistId: Long) : NowPlayingEvent()
    data class CreatePlaylistAndAdd(val name: String) : NowPlayingEvent()
}
