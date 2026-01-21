package com.pulse.music.domain.repository

interface AudioEffectController {
    fun init(audioSessionId: Int)
    fun release()
    
    // Equalizer
    fun setEqualizerEnabled(enabled: Boolean)
    fun isEqualizerEnabled(): Boolean
    fun getNumberOfBands(): Short
    fun getCenterFreq(band: Short): Int
    fun getBandLevelRange(): ShortArray
    fun setBandLevel(band: Short, level: Short)
    fun getBandLevel(band: Short): Short
    fun getPresetCount(): Short
    fun getPresetName(preset: Short): String
    fun usePreset(preset: Short)
    
    // Bass Boost
    fun setBassBoostEnabled(enabled: Boolean)
    fun isBassBoostEnabled(): Boolean
    fun isBassBoostSupported(): Boolean
    fun setBassBoostStrength(strength: Short)
    fun getBassBoostStrength(): Short
    
    // Virtualizer
    fun setVirtualizerEnabled(enabled: Boolean)
    fun isVirtualizerEnabled(): Boolean
    fun isVirtualizerSupported(): Boolean
    fun setVirtualizerStrength(strength: Short)
    fun getVirtualizerStrength(): Short
    
    // Loudness
    fun setLoudnessEnabled(enabled: Boolean)
    fun isLoudnessEnabled(): Boolean
    fun isLoudnessSupported(): Boolean
    fun setLoudnessGain(gainMb: Int)
    fun getLoudnessGain(): Int
}
