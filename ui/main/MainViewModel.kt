package com.sigma.music.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigma.music.domain.model.MusicState
import com.sigma.music.domain.usecase.GetMusicStateUseCase
import com.sigma.music.domain.usecase.GetPlaybackProgressUseCase
import com.sigma.music.domain.usecase.PlayQueueItemUseCase
import com.sigma.music.domain.usecase.RemoveQueueItemUseCase
import com.sigma.music.domain.usecase.TogglePlayPauseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getMusicStateUseCase: GetMusicStateUseCase,
    getPlaybackProgressUseCase: GetPlaybackProgressUseCase,
    private val togglePlayPauseUseCase: TogglePlayPauseUseCase,
    private val playQueueItemUseCase: PlayQueueItemUseCase,
    private val removeQueueItemUseCase: RemoveQueueItemUseCase
) : ViewModel() {

    val musicState: StateFlow<MusicState> = getMusicStateUseCase()

    val progress: StateFlow<Float> = getPlaybackProgressUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0f
        )

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
