package com.gemini.music.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.SleepTimerMode
import com.gemini.music.domain.model.SleepTimerState
import com.gemini.music.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepTimerViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _timerState = MutableStateFlow(SleepTimerState())
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()
    
    private var timerJob: Job? = null
    
    // Callback for when timer finishes
    var onTimerFinished: (() -> Unit)? = null
    var onFadeVolume: ((Float) -> Unit)? = null
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val fadeOut = userPreferencesRepository.sleepTimerFadeOut.first()
            val fadeDuration = userPreferencesRepository.sleepTimerFadeDuration.first()
            
            _timerState.update {
                it.copy(
                    fadeOut = fadeOut,
                    fadeDurationSeconds = fadeDuration
                )
            }
        }
    }
    
    fun startTimer(durationMs: Long) {
        cancelTimer()
        
        _timerState.update {
            it.copy(
                mode = SleepTimerMode.DURATION,
                isActive = true,
                remainingTimeMs = durationMs,
                originalDurationMs = durationMs
            )
        }
        
        timerJob = viewModelScope.launch {
            while (_timerState.value.remainingTimeMs > 0) {
                delay(1000)
                
                _timerState.update {
                    it.copy(remainingTimeMs = it.remainingTimeMs - 1000)
                }
                
                // Handle fade out
                val state = _timerState.value
                if (state.fadeOut && state.remainingTimeMs <= state.fadeDurationSeconds * 1000) {
                    val fadeProgress = state.remainingTimeMs.toFloat() / (state.fadeDurationSeconds * 1000)
                    onFadeVolume?.invoke(fadeProgress)
                }
            }
            
            // Timer finished
            _timerState.update {
                it.copy(
                    mode = SleepTimerMode.OFF,
                    isActive = false,
                    remainingTimeMs = 0
                )
            }
            onTimerFinished?.invoke()
        }
    }
    
    fun startTrackCountTimer(trackCount: Int) {
        cancelTimer()
        
        _timerState.update {
            it.copy(
                mode = SleepTimerMode.TRACKS,
                isActive = true,
                remainingTracks = trackCount,
                originalTrackCount = trackCount
            )
        }
    }
    
    fun startEndOfTrackTimer() {
        cancelTimer()
        
        _timerState.update {
            it.copy(
                mode = SleepTimerMode.END_OF_TRACK,
                isActive = true,
                remainingTracks = 1,
                originalTrackCount = 1
            )
        }
    }
    
    fun onTrackChanged() {
        val state = _timerState.value
        
        when (state.mode) {
            SleepTimerMode.END_OF_TRACK -> {
                // Stop playback after current track
                handleFadeAndStop()
            }
            SleepTimerMode.TRACKS -> {
                val newCount = state.remainingTracks - 1
                if (newCount <= 0) {
                    handleFadeAndStop()
                } else {
                    _timerState.update { it.copy(remainingTracks = newCount) }
                }
            }
            else -> { /* No action needed */ }
        }
    }
    
    private fun handleFadeAndStop() {
        val state = _timerState.value
        
        if (state.fadeOut) {
            viewModelScope.launch {
                val fadeDurationMs = state.fadeDurationSeconds * 1000L
                val steps = 30
                val stepDelay = fadeDurationMs / steps
                
                for (i in steps downTo 0) {
                    val volume = i.toFloat() / steps
                    onFadeVolume?.invoke(volume)
                    delay(stepDelay)
                }
                
                finishTimer()
            }
        } else {
            finishTimer()
        }
    }
    
    private fun finishTimer() {
        _timerState.update {
            it.copy(
                mode = SleepTimerMode.OFF,
                isActive = false,
                remainingTimeMs = 0,
                remainingTracks = 0
            )
        }
        onTimerFinished?.invoke()
    }
    
    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        
        _timerState.update {
            it.copy(
                mode = SleepTimerMode.OFF,
                isActive = false,
                remainingTimeMs = 0,
                remainingTracks = 0
            )
        }
        
        // Restore volume
        onFadeVolume?.invoke(1f)
    }
    
    fun addTime(minutes: Int) {
        if (_timerState.value.mode == SleepTimerMode.DURATION) {
            _timerState.update {
                it.copy(
                    remainingTimeMs = it.remainingTimeMs + (minutes * 60 * 1000),
                    originalDurationMs = it.originalDurationMs + (minutes * 60 * 1000)
                )
            }
        }
    }
    
    fun addTracks(count: Int) {
        if (_timerState.value.mode == SleepTimerMode.TRACKS) {
            _timerState.update {
                it.copy(
                    remainingTracks = it.remainingTracks + count,
                    originalTrackCount = it.originalTrackCount + count
                )
            }
        }
    }
    
    fun setFadeOut(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSleepTimerFadeOut(enabled)
            _timerState.update { it.copy(fadeOut = enabled) }
        }
    }
    
    fun setFadeDuration(seconds: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setSleepTimerFadeDuration(seconds)
            _timerState.update { it.copy(fadeDurationSeconds = seconds) }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
