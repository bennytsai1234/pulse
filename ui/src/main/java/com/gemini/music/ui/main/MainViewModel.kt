package com.gemini.music.ui.main

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.MusicState
import com.gemini.music.domain.usecase.GetMusicStateUseCase
import com.gemini.music.domain.usecase.GetPlaybackProgressUseCase
import com.gemini.music.domain.usecase.PlayQueueItemUseCase
import com.gemini.music.domain.usecase.RemoveQueueItemUseCase
import com.gemini.music.domain.usecase.TogglePlayPauseUseCase
import com.gemini.music.domain.usecase.GetSongWaveformUseCase
import com.gemini.music.ui.theme.DynamicThemeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getSongWaveformUseCase: GetSongWaveformUseCase
) : ViewModel() {

    val musicState: StateFlow<MusicState> = getMusicStateUseCase()

    val progress: StateFlow<Float> = getPlaybackProgressUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0f
        )

    private val _waveform = MutableStateFlow<List<Float>>(emptyList())
    val waveform: StateFlow<List<Float>> = _waveform.asStateFlow()

    // Dynamic Theme State - 全局動態主題
    private val _dynamicThemeState = MutableStateFlow(DynamicThemeState())
    val dynamicThemeState: StateFlow<DynamicThemeState> = _dynamicThemeState.asStateFlow()

    private var currentSongDataPath: String? = null

    init {
        viewModelScope.launch {
            musicState.collect { state ->
                val song = state.currentSong
                if (song != null && song.dataPath != currentSongDataPath) {
                    currentSongDataPath = song.dataPath
                    _waveform.value = emptyList() // Clear previous
                    try {
                        val rawWaveform = getSongWaveformUseCase(song.dataPath)
                        val maxValue = rawWaveform.maxOrNull() ?: 1
                        _waveform.value = rawWaveform.map { it.toFloat() / maxValue }
                    } catch (e: Exception) {
                        _waveform.value = emptyList()
                    }
                }
            }
        }
    }

    /**
     * 更新動態主題顏色 - 從專輯封面 Bitmap 提取
     */
    fun updateDynamicTheme(bitmap: Bitmap?) {
        viewModelScope.launch {
            _dynamicThemeState.value.extractColorsFromBitmap(bitmap)
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

