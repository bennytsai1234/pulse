@file:Suppress("DEPRECATION")

package com.pulse.music.ui.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.music.domain.model.CustomEqPreset
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
    private val eqPresetRepository: EqPresetRepository
) : ViewModel() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

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
                // Release previous instances
                equalizer?.release()
                bassBoost?.release()
                virtualizer?.release()

                // Initialize Equalizer
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = false
                }

                val eq = equalizer ?: return@launch

                // Load saved settings
                val savedEnabled = userPreferencesRepository.equalizerEnabled.first()
                val savedBandLevels = userPreferencesRepository.equalizerBandLevels.first()
                val savedPresetIndex = userPreferencesRepository.equalizerPresetIndex.first()

                // Apply saved preset if available
                if (savedPresetIndex >= 0 && savedPresetIndex < eq.numberOfPresets) {
                    eq.usePreset(savedPresetIndex.toShort())
                } else if (savedBandLevels.isNotEmpty()) {
                    // Apply saved band levels
                    savedBandLevels.forEachIndexed { index, level ->
                        if (index < eq.numberOfBands) {
                            eq.setBandLevel(index.toShort(), level.toShort())
                        }
                    }
                }

                eq.enabled = savedEnabled

                val bands = (0 until eq.numberOfBands).map { i ->
                    val bandIndex = i.toShort()
                    EqualizerBand(
                        index = i,
                        centerFrequency = eq.getCenterFreq(bandIndex) / 1000,
                        minLevel = eq.bandLevelRange[0].toInt(),
                        maxLevel = eq.bandLevelRange[1].toInt(),
                        currentLevel = eq.getBandLevel(bandIndex).toInt()
                    )
                }

                val presets = (0 until eq.numberOfPresets).map { i ->
                    EqualizerPreset(
                        index = i,
                        name = eq.getPresetName(i.toShort())
                    )
                }

                // Initialize Bass Boost
                var bassBoostAvailable = false
                val savedBassBoostEnabled = userPreferencesRepository.bassBoostEnabled.first()
                val savedBassBoostStrength = userPreferencesRepository.bassBoostStrength.first()
                try {
                    bassBoost = BassBoost(0, audioSessionId).apply {
                        enabled = savedBassBoostEnabled
                        if (strengthSupported) {
                            setStrength(savedBassBoostStrength.toShort())
                        }
                    }
                    bassBoostAvailable = bassBoost?.strengthSupported == true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Initialize Virtualizer
                var virtualizerAvailable = false
                val savedVirtualizerEnabled = userPreferencesRepository.virtualizerEnabled.first()
                val savedVirtualizerStrength = userPreferencesRepository.virtualizerStrength.first()
                try {
                    virtualizer = Virtualizer(0, audioSessionId).apply {
                        enabled = savedVirtualizerEnabled
                        if (strengthSupported) {
                            setStrength(savedVirtualizerStrength.toShort())
                        }
                    }
                    virtualizerAvailable = virtualizer?.strengthSupported == true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Initialize Loudness Enhancer (API 19+)
                var loudnessAvailable = false
                val savedLoudnessEnabled = userPreferencesRepository.loudnessEnabled.first()
                val savedLoudnessGain = userPreferencesRepository.loudnessGain.first()
                try {
                    loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                        enabled = savedLoudnessEnabled
                        setTargetGain(savedLoudnessGain)
                        // Note: LoudnessEnhancer gain is in millibel, we store as 0-1000 range
                    }
                    loudnessAvailable = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                _uiState.update {
                    it.copy(
                        isEnabled = eq.enabled,
                        bands = bands,
                        presets = presets,
                        currentPresetIndex = savedPresetIndex,
                        isAvailable = true,
                        errorMessage = null,
                        bassBoostAvailable = bassBoostAvailable,
                        bassBoostEnabled = bassBoost?.enabled == true,
                        bassBoostStrength = savedBassBoostStrength,
                        virtualizerAvailable = virtualizerAvailable,
                        virtualizerEnabled = virtualizer?.enabled == true,
                        virtualizerStrength = savedVirtualizerStrength,
                        loudnessAvailable = loudnessAvailable,
                        loudnessEnabled = loudnessEnhancer?.enabled == true,
                        loudnessGain = savedLoudnessGain
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
        equalizer?.let { eq ->
            eq.enabled = enabled
            _uiState.update { it.copy(isEnabled = enabled) }
            viewModelScope.launch {
                userPreferencesRepository.setEqualizerEnabled(enabled)
            }
        }
    }

    fun setBandLevel(bandIndex: Int, normalizedLevel: Float) {
        equalizer?.let { eq ->
            val band = _uiState.value.bands.getOrNull(bandIndex) ?: return
            val level = (band.minLevel + (band.maxLevel - band.minLevel) * normalizedLevel).toInt()

            eq.setBandLevel(bandIndex.toShort(), level.toShort())

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
    }

    fun selectPreset(presetIndex: Int) {
        equalizer?.let { eq ->
            eq.usePreset(presetIndex.toShort())

            val bands = _uiState.value.bands.map { band ->
                band.copy(currentLevel = eq.getBandLevel(band.index.toShort()).toInt())
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
    }

    fun resetToFlat() {
        equalizer?.let { eq ->
            val midLevel = ((eq.bandLevelRange[0] + eq.bandLevelRange[1]) / 2).toShort()

            for (i in 0 until eq.numberOfBands) {
                eq.setBandLevel(i.toShort(), midLevel)
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
                // Also reset loudness settings
                setLoudnessEnabled(false)
                setLoudnessGain(0)
            }
        }
    }

    // ==================== Bass Boost ====================

    fun setBassBoostEnabled(enabled: Boolean) {
        bassBoost?.let { bb ->
            bb.enabled = enabled
            _uiState.update { it.copy(bassBoostEnabled = enabled) }
            viewModelScope.launch {
                userPreferencesRepository.setBassBoostEnabled(enabled)
            }
        }
    }

    fun setBassBoostStrength(strength: Int) {
        bassBoost?.let { bb ->
            val clampedStrength = strength.coerceIn(0, 1000).toShort()
            bb.setStrength(clampedStrength)
            _uiState.update { it.copy(bassBoostStrength = clampedStrength.toInt()) }
            viewModelScope.launch {
                userPreferencesRepository.setBassBoostStrength(clampedStrength.toInt())
            }
        }
    }

    // ==================== Virtualizer ====================

    fun setVirtualizerEnabled(enabled: Boolean) {
        virtualizer?.let { virt ->
            virt.enabled = enabled
            _uiState.update { it.copy(virtualizerEnabled = enabled) }
            viewModelScope.launch {
                userPreferencesRepository.setVirtualizerEnabled(enabled)
            }
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        virtualizer?.let { virt ->
            val clampedStrength = strength.coerceIn(0, 1000).toShort()
            virt.setStrength(clampedStrength)
            _uiState.update { it.copy(virtualizerStrength = clampedStrength.toInt()) }
            viewModelScope.launch {
                userPreferencesRepository.setVirtualizerStrength(clampedStrength.toInt())
            }
        }
    }

    // ==================== Loudness Enhancer ====================

    fun setLoudnessEnabled(enabled: Boolean) {
        loudnessEnhancer?.let { le ->
            le.enabled = enabled
            _uiState.update { it.copy(loudnessEnabled = enabled) }
            viewModelScope.launch {
                userPreferencesRepository.setLoudnessEnabled(enabled)
            }
        }
    }

    fun setLoudnessGain(gainMb: Int) {
        loudnessEnhancer?.let { le ->
            val clampedGain = gainMb.coerceIn(0, 1000)
            le.setTargetGain(clampedGain)
            _uiState.update { it.copy(loudnessGain = clampedGain) }
            viewModelScope.launch {
                userPreferencesRepository.setLoudnessGain(clampedGain)
            }
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
        equalizer?.let { eq ->
            // Apply band levels
            preset.bandLevels.forEachIndexed { index, level ->
                if (index < eq.numberOfBands) {
                    eq.setBandLevel(index.toShort(), level.toShort())
                }
            }

            val bands = _uiState.value.bands.mapIndexed { index, band ->
                if (index < preset.bandLevels.size) {
                    band.copy(currentLevel = preset.bandLevels[index])
                } else band
            }

            // Apply Bass Boost
            bassBoost?.let { bb ->
                if (bb.strengthSupported) {
                    bb.setStrength(preset.bassBoostStrength.toShort())
                }
            }

            // Apply Virtualizer
            virtualizer?.let { virt ->
                if (virt.strengthSupported) {
                    virt.setStrength(preset.virtualizerStrength.toShort())
                }
            }

            // Apply Loudness
            loudnessEnhancer?.setTargetGain(preset.loudnessGain)

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
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }
}




