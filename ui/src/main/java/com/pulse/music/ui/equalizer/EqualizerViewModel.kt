@file:Suppress("DEPRECATION")

package com.pulse.music.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.music.domain.model.CustomEqPreset
import com.pulse.music.domain.repository.AudioEffectController
import com.pulse.music.domain.repository.EqPresetRepository
import com.pulse.music.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EqualizerBand(
    val index: Int,
    val centerFrequency: Int, // Hz
    val minLevel: Int, // millibel
    val maxLevel: Int, // millibel
    val currentLevel: Int // millibel
) {
    val label: String
        get() = when {
            centerFrequency >= 1000 -> "${centerFrequency / 1000}k"
            else -> "$centerFrequency"
        }

    // Normalized level between 0 and 1
    val normalizedLevel: Float
        get() = (currentLevel - minLevel).toFloat() / (maxLevel - minLevel).toFloat()
}

data class EqualizerPreset(
    val index: Int,
    val name: String,
    val isCustom: Boolean = false,
    val customId: Long = 0
)

data class EqualizerUiState(
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<EqualizerPreset> = emptyList(),
    val customPresets: List<CustomEqPreset> = emptyList(),
    val currentPresetIndex: Int = -1,
    val currentCustomPresetId: Long = 0,
    val isAvailable: Boolean = false,
    val errorMessage: String? = null,
    // Bass Boost
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0, // 0-1000
    val bassBoostAvailable: Boolean = false,
    // Virtualizer
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0, // 0-1000
    val virtualizerAvailable: Boolean = false,
    // Loudness Enhancer
    val loudnessEnabled: Boolean = false,
    val loudnessGain: Int = 0, // millibel (0-1000 mb = 0-10 dB)
    val loudnessAvailable: Boolean = false,
    // UI States
    val showSavePresetDialog: Boolean = false,
    val presetNameInput: String = ""
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val eqPresetRepository: EqPresetRepository,
    private val audioEffectController: AudioEffectController
) : ViewModel() {

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    init {
        loadCustomPresets()
    }

    private fun loadCustomPresets() {
        viewModelScope.launch {
            eqPresetRepository.getAllPresets().collect { presets ->
                _uiState.update { it.copy(customPresets = presets) }
            }
        }
    }

    fun initializeEqualizer(audioSessionId: Int) {
        viewModelScope.launch {
            try {
                audioEffectController.init(audioSessionId)

                // Load saved settings
                val savedEnabled = userPreferencesRepository.equalizerEnabled.first()
                val savedBandLevels = userPreferencesRepository.equalizerBandLevels.first()
                val savedPresetIndex = userPreferencesRepository.equalizerPresetIndex.first()

                // Apply saved preset if available
                if (savedPresetIndex >= 0 && savedPresetIndex < audioEffectController.getPresetCount()) {
                    audioEffectController.usePreset(savedPresetIndex.toShort())
                } else if (savedBandLevels.isNotEmpty()) {
                    // Apply saved band levels
                    savedBandLevels.forEachIndexed { index, level ->
                        if (index < audioEffectController.getNumberOfBands()) {
                            audioEffectController.setBandLevel(index.toShort(), level.toShort())
                        }
                    }
                }

                audioEffectController.setEqualizerEnabled(savedEnabled)

                val numBands = audioEffectController.getNumberOfBands()
                val bands = (0 until numBands).map { i ->
                    val bandIndex = i.toShort()
                    val range = audioEffectController.getBandLevelRange()
                    EqualizerBand(
                        index = i,
                        centerFrequency = audioEffectController.getCenterFreq(bandIndex) / 1000,
                        minLevel = range[0].toInt(),
                        maxLevel = range[1].toInt(),
                        currentLevel = audioEffectController.getBandLevel(bandIndex).toInt()
                    )
                }

                val numPresets = audioEffectController.getPresetCount()
                val presets = (0 until numPresets).map { i ->
                    EqualizerPreset(
                        index = i,
                        name = audioEffectController.getPresetName(i.toShort())
                    )
                }

                // Initialize Bass Boost
                val savedBassBoostEnabled = userPreferencesRepository.bassBoostEnabled.first()
                val savedBassBoostStrength = userPreferencesRepository.bassBoostStrength.first()
                audioEffectController.setBassBoostEnabled(savedBassBoostEnabled)
                if (audioEffectController.isBassBoostSupported()) {
                    audioEffectController.setBassBoostStrength(savedBassBoostStrength.toShort())
                }

                // Initialize Virtualizer
                val savedVirtualizerEnabled = userPreferencesRepository.virtualizerEnabled.first()
                val savedVirtualizerStrength = userPreferencesRepository.virtualizerStrength.first()
                audioEffectController.setVirtualizerEnabled(savedVirtualizerEnabled)
                if (audioEffectController.isVirtualizerSupported()) {
                    audioEffectController.setVirtualizerStrength(savedVirtualizerStrength.toShort())
                }
                
                // Initialize Loudness
                val savedLoudnessEnabled = userPreferencesRepository.loudnessEnabled.first()
                val savedLoudnessGain = userPreferencesRepository.loudnessGain.first()
                audioEffectController.setLoudnessEnabled(savedLoudnessEnabled)
                audioEffectController.setLoudnessGain(savedLoudnessGain)
                
                _uiState.update {
                    it.copy(
                        isEnabled = audioEffectController.isEqualizerEnabled(),
                        bands = bands,
                        presets = presets,
                        currentPresetIndex = savedPresetIndex,
                        isAvailable = true,
                        errorMessage = null,
                        bassBoostAvailable = audioEffectController.isBassBoostSupported(),
                        bassBoostEnabled = audioEffectController.isBassBoostEnabled(),
                        bassBoostStrength = audioEffectController.getBassBoostStrength().toInt(),
                        virtualizerAvailable = audioEffectController.isVirtualizerSupported(),
                        virtualizerEnabled = audioEffectController.isVirtualizerEnabled(),
                        virtualizerStrength = audioEffectController.getVirtualizerStrength().toInt(),
                        loudnessAvailable = audioEffectController.isLoudnessSupported(),
                        loudnessEnabled = audioEffectController.isLoudnessEnabled(),
                        loudnessGain = audioEffectController.getLoudnessGain()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isAvailable = false,
                        errorMessage = "Equalizer not available: ${e.message}"
                    )
                }
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        audioEffectController.setEqualizerEnabled(enabled)
        _uiState.update { it.copy(isEnabled = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setEqualizerEnabled(enabled)
        }
    }

    fun setBandLevel(bandIndex: Int, normalizedLevel: Float) {
        val band = _uiState.value.bands.getOrNull(bandIndex) ?: return
        val level = (band.minLevel + (band.maxLevel - band.minLevel) * normalizedLevel).toInt()

        audioEffectController.setBandLevel(bandIndex.toShort(), level.toShort())

        _uiState.update { state ->
            state.copy(
                bands = state.bands.map {
                    if (it.index == bandIndex) it.copy(currentLevel = level) else it
                },
                currentPresetIndex = -1
            )
        }

        // Save band levels
        viewModelScope.launch {
            userPreferencesRepository.setEqualizerBandLevels(
                _uiState.value.bands.map { it.currentLevel }
            )
            userPreferencesRepository.setEqualizerPresetIndex(-1)
        }
    }

    fun selectPreset(presetIndex: Int) {
        audioEffectController.usePreset(presetIndex.toShort())

        val bands = _uiState.value.bands.map { band ->
            band.copy(currentLevel = audioEffectController.getBandLevel(band.index.toShort()).toInt())
        }

        _uiState.update {
            it.copy(
                bands = bands,
                currentPresetIndex = presetIndex
            )
        }

        // Save preset selection
        viewModelScope.launch {
            userPreferencesRepository.setEqualizerPresetIndex(presetIndex)
            userPreferencesRepository.setEqualizerBandLevels(bands.map { it.currentLevel })
        }
    }

    fun resetToFlat() {
        val range = audioEffectController.getBandLevelRange()
        val midLevel = ((range[0] + range[1]) / 2).toShort()
        val numBands = audioEffectController.getNumberOfBands()

        for (i in 0 until numBands) {
            audioEffectController.setBandLevel(i.toShort(), midLevel)
        }

        val bands = _uiState.value.bands.map {
            it.copy(currentLevel = midLevel.toInt())
        }

        _uiState.update {
            it.copy(bands = bands, currentPresetIndex = -1)
        }

        // Save reset state
        viewModelScope.launch {
            userPreferencesRepository.setEqualizerPresetIndex(-1)
            userPreferencesRepository.setEqualizerBandLevels(bands.map { it.currentLevel })
        }
    }

    // ==================== Bass Boost ====================

    fun setBassBoostEnabled(enabled: Boolean) {
        audioEffectController.setBassBoostEnabled(enabled)
        _uiState.update { it.copy(bassBoostEnabled = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setBassBoostEnabled(enabled)
        }
    }

    fun setBassBoostStrength(strength: Int) {
        val clampedStrength = strength.coerceIn(0, 1000).toShort()
        audioEffectController.setBassBoostStrength(clampedStrength)
        _uiState.update { it.copy(bassBoostStrength = clampedStrength.toInt()) }
        viewModelScope.launch {
            userPreferencesRepository.setBassBoostStrength(clampedStrength.toInt())
        }
    }

    // ==================== Virtualizer ====================

    fun setVirtualizerEnabled(enabled: Boolean) {
        audioEffectController.setVirtualizerEnabled(enabled)
        _uiState.update { it.copy(virtualizerEnabled = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setVirtualizerEnabled(enabled)
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        val clampedStrength = strength.coerceIn(0, 1000).toShort()
        audioEffectController.setVirtualizerStrength(clampedStrength)
        _uiState.update { it.copy(virtualizerStrength = clampedStrength.toInt()) }
        viewModelScope.launch {
            userPreferencesRepository.setVirtualizerStrength(clampedStrength.toInt())
        }
    }

    // ==================== Loudness Enhancer ====================
    
    fun setLoudnessEnabled(enabled: Boolean) {
        audioEffectController.setLoudnessEnabled(enabled)
        _uiState.update { it.copy(loudnessEnabled = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setLoudnessEnabled(enabled)
        }
    }

    fun setLoudnessGain(gainMb: Int) {
        val clampedGain = gainMb.coerceIn(0, 1000)
        audioEffectController.setLoudnessGain(clampedGain)
        _uiState.update { it.copy(loudnessGain = clampedGain) }
        viewModelScope.launch {
            userPreferencesRepository.setLoudnessGain(clampedGain)
        }
    }

    // ==================== Custom Presets ====================

    fun showSavePresetDialog() {
        _uiState.update { it.copy(showSavePresetDialog = true, presetNameInput = "") }
    }

    fun hideSavePresetDialog() {
        _uiState.update { it.copy(showSavePresetDialog = false, presetNameInput = "") }
    }

    fun updatePresetNameInput(name: String) {
        _uiState.update { it.copy(presetNameInput = name) }
    }

    fun saveCurrentAsPreset() {
        val name = _uiState.value.presetNameInput.trim()
        if (name.isEmpty()) return

        val state = _uiState.value
        val preset = CustomEqPreset(
            name = name,
            bandLevels = state.bands.map { it.currentLevel },
            bassBoostStrength = state.bassBoostStrength,
            virtualizerStrength = state.virtualizerStrength,
            loudnessGain = state.loudnessGain
        )

        viewModelScope.launch {
            eqPresetRepository.savePreset(preset)
            _uiState.update { it.copy(showSavePresetDialog = false, presetNameInput = "") }
        }
    }

    fun applyCustomPreset(preset: CustomEqPreset) {
        // Apply band levels
        preset.bandLevels.forEachIndexed { index, level ->
            if (index < audioEffectController.getNumberOfBands()) {
                audioEffectController.setBandLevel(index.toShort(), level.toShort())
            }
        }

        val bands = _uiState.value.bands.mapIndexed { index, band ->
            if (index < preset.bandLevels.size) {
                band.copy(currentLevel = preset.bandLevels[index])
            } else band
        }

        // Apply Bass Boost
        if (audioEffectController.isBassBoostSupported()) {
            audioEffectController.setBassBoostStrength(preset.bassBoostStrength.toShort())
        }

        // Apply Virtualizer
        if (audioEffectController.isVirtualizerSupported()) {
            audioEffectController.setVirtualizerStrength(preset.virtualizerStrength.toShort())
        }

        // Apply Loudness
        audioEffectController.setLoudnessGain(preset.loudnessGain)

        _uiState.update {
            it.copy(
                bands = bands,
                currentPresetIndex = -1,
                currentCustomPresetId = preset.id,
                bassBoostStrength = preset.bassBoostStrength,
                virtualizerStrength = preset.virtualizerStrength,
                loudnessGain = preset.loudnessGain
            )
        }

        // Save to preferences
        viewModelScope.launch {
            userPreferencesRepository.setEqualizerPresetIndex(-1)
            userPreferencesRepository.setEqualizerBandLevels(preset.bandLevels)
            userPreferencesRepository.setBassBoostStrength(preset.bassBoostStrength)
            userPreferencesRepository.setVirtualizerStrength(preset.virtualizerStrength)
        }
    }

    fun deleteCustomPreset(preset: CustomEqPreset) {
        viewModelScope.launch {
            eqPresetRepository.deletePreset(preset.id)
            if (_uiState.value.currentCustomPresetId == preset.id) {
                _uiState.update { it.copy(currentCustomPresetId = 0) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Do NOT release controller here, as it is a Singleton injected
    }
}




