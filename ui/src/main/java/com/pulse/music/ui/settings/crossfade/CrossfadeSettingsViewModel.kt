package com.pulse.music.ui.settings.crossfade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.music.domain.model.CrossfadeCurve
import com.pulse.music.domain.usecase.crossfade.GetCrossfadeSettingsUseCase
import com.pulse.music.domain.usecase.crossfade.UpdateCrossfadeSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 交叉淡入淡出設定 ViewModel
 */
@HiltViewModel
class CrossfadeSettingsViewModel @Inject constructor(
    private val getCrossfadeSettings: GetCrossfadeSettingsUseCase,
    private val updateCrossfadeSettings: UpdateCrossfadeSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrossfadeSettingsUiState())
    val uiState: StateFlow<CrossfadeSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getCrossfadeSettings().collect { settings ->
                _uiState.update {
                    CrossfadeSettingsUiState.fromSettings(settings)
                }
            }
        }
    }

    fun onEvent(event: CrossfadeSettingsUiEvent) {
        when (event) {
            is CrossfadeSettingsUiEvent.SetEnabled -> setEnabled(event.enabled)
            is CrossfadeSettingsUiEvent.SetDuration -> setDuration(event.seconds)
            is CrossfadeSettingsUiEvent.SetCurve -> setCurve(event.curve)
            is CrossfadeSettingsUiEvent.SetApplyOnManualSkip -> setApplyOnManualSkip(event.apply)
            is CrossfadeSettingsUiEvent.SetAlbumContinuous -> setAlbumContinuous(event.enabled)
            is CrossfadeSettingsUiEvent.SetSilenceDetection -> setSilenceDetection(event.enabled)
            CrossfadeSettingsUiEvent.ToggleAdvancedSettings -> toggleAdvancedSettings()
        }
    }

    private fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateCrossfadeSettings.setEnabled(enabled)
            _uiState.update { it.copy(enabled = enabled) }
        }
    }

    private fun setDuration(seconds: Int) {
        viewModelScope.launch {
            updateCrossfadeSettings.setDurationSeconds(seconds)
            _uiState.update { it.copy(durationSeconds = seconds) }
        }
    }

    private fun setCurve(curve: CrossfadeCurve) {
        viewModelScope.launch {
            updateCrossfadeSettings.setCurve(curve)
            _uiState.update { it.copy(curve = curve) }
        }
    }

    private fun setApplyOnManualSkip(apply: Boolean) {
        viewModelScope.launch {
            updateCrossfadeSettings.setApplyOnManualSkip(apply)
            _uiState.update { it.copy(applyOnManualSkip = apply) }
        }
    }

    private fun setAlbumContinuous(enabled: Boolean) {
        viewModelScope.launch {
            updateCrossfadeSettings.setAlbumContinuous(enabled)
            _uiState.update { it.copy(albumContinuous = enabled) }
        }
    }

    private fun setSilenceDetection(enabled: Boolean) {
        viewModelScope.launch {
            updateCrossfadeSettings.setSilenceDetection(enabled)
            _uiState.update { it.copy(silenceDetection = enabled) }
        }
    }

    private fun toggleAdvancedSettings() {
        _uiState.update { it.copy(showAdvancedSettings = !it.showAdvancedSettings) }
    }
}
