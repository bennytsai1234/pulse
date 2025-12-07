package com.gemini.music.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.MusicState
import com.gemini.music.domain.usecase.GetMusicStateUseCase
import com.gemini.music.domain.usecase.GetPlaybackProgressUseCase
import com.gemini.music.domain.usecase.PlayQueueItemUseCase
import com.gemini.music.domain.usecase.RemoveQueueItemUseCase
import com.gemini.music.domain.usecase.TogglePlayPauseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getMusicStateUseCase: GetMusicStateUseCase,
    getPlaybackProgressUseCase: GetPlaybackProgressUseCase,
    private val togglePlayPauseUseCase: TogglePlayPauseUseCase,
    private val playQueueItemUseCase: PlayQueueItemUseCase,
    private val removeQueueItemUseCase: RemoveQueueItemUseCase,
    private val audioWaveformExtractor: com.gemini.music.data.source.AudioWaveformExtractor
) : ViewModel() {

    val musicState: StateFlow<MusicState> = getMusicStateUseCase()

    val progress: StateFlow<Float> = getPlaybackProgressUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0f
        )

    private val _waveform = kotlinx.coroutines.flow.MutableStateFlow<List<Float>>(emptyList())
    val waveform: StateFlow<List<Float>> = _waveform

    private var currentSongUri: String? = null

    init {
        viewModelScope.launch {
            musicState.collect { state ->
                val song = state.currentSong
                if (song != null && song.contentUri != currentSongUri) {
                    currentSongUri = song.contentUri
                    _waveform.value = emptyList() // Clear previous
                     val uri = android.net.Uri.parse(song.contentUri)
                     val ints = audioWaveformExtractor.extractWaveform(uri)
                     _waveform.value = ints.map { it / 100f } // Normalize 0..1
                }
            }
        }
    }

    fun togglePlayPause() {
        togglePlayPauseUseCase()
    }

    fun playQueueItem(index: Int) {
        playQueueItemUseCase(index)
    }

    fun removeQueueItem(index: Int) {
        removeQueueItemUseCase(index)
    }
}
