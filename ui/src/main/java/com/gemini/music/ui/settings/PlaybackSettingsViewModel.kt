package com.gemini.music.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaybackSettingsUiState(
    val playbackSpeed: Float = 1.0f,
    val crossfadeDuration: Int = 0,
    val sleepTimerFadeOut: Boolean = true,
    val sleepTimerFadeDuration: Int = 30,
    val isLoading: Boolean = true
)

@HiltViewModel
class PlaybackSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlaybackSettingsUiState())
    val uiState: StateFlow<PlaybackSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val speed = userPreferencesRepository.playbackSpeed.first()
            val crossfade = userPreferencesRepository.crossfadeDuration.first()
            val fadeOut = userPreferencesRepository.sleepTimerFadeOut.first()
            val fadeDuration = userPreferencesRepository.sleepTimerFadeDuration.first()
            
            _uiState.update {
                it.copy(
                    playbackSpeed = speed,
                    crossfadeDuration = crossfade,
                    sleepTimerFadeOut = fadeOut,
                    sleepTimerFadeDuration = fadeDuration,
                    isLoading = false
                )
            }
        }
    }
    
    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setPlaybackSpeed(speed)
            _uiState.update { it.copy(playbackSpeed = speed) }
        }
    }
    
    fun setCrossfadeDuration(seconds: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setCrossfadeDuration(seconds)
            _uiState.update { it.copy(crossfadeDuration = seconds) }
        }
    }
    
    fun setSleepTimerFadeOut(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSleepTimerFadeOut(enabled)
            _uiState.update { it.copy(sleepTimerFadeOut = enabled) }
        }
    }
    
    fun setSleepTimerFadeDuration(seconds: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setSleepTimerFadeDuration(seconds)
            _uiState.update { it.copy(sleepTimerFadeDuration = seconds) }
        }
    }
    
    // ==================== Playback Speed Presets ====================
    
    companion object {
        val SPEED_PRESETS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        val CROSSFADE_OPTIONS = listOf(0, 2, 4, 6, 8, 10, 12) // seconds
        val FADE_DURATION_OPTIONS = listOf(5, 10, 15, 30, 45, 60) // seconds
    }
}
