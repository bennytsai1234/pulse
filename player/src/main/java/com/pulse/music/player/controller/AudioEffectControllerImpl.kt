package com.pulse.music.player.controller

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import com.pulse.music.domain.repository.AudioEffectController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("DEPRECATION")
class AudioEffectControllerImpl @Inject constructor() : AudioEffectController {

    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var isSafeMode = false

    override fun init(audioSessionId: Int) {
        if (isSafeMode) return
        release()
        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = false
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = false
            }
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true // Equalizer is usually always "enabled" but flat
            }
            // LoudnessEnhancer is API 19+, but we are minSdk 26 so it's safe
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = false
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioEffectController", "Failed to initialize audio effects. Entering Safe Mode.", e)
            isSafeMode = true
            release()
        }
    }

    override fun release() {
        bassBoost?.release()
        bassBoost = null
        virtualizer?.release()
        virtualizer = null
        equalizer?.release()
        equalizer = null
        loudnessEnhancer?.release()
        loudnessEnhancer = null
    }

    // --- Equalizer ---
    
    override fun setEqualizerEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
    }

    override fun isEqualizerEnabled(): Boolean = equalizer?.enabled ?: false

    override fun getNumberOfBands(): Short = equalizer?.numberOfBands ?: 0

    override fun getCenterFreq(band: Short): Int = equalizer?.getCenterFreq(band) ?: 0

    override fun getBandLevelRange(): ShortArray = equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)

    override fun setBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
        } catch (e: Exception) {
            android.util.Log.e("AudioEffectController", "Error setting band level", e)
        }
    }

    override fun getBandLevel(band: Short): Short = equalizer?.getBandLevel(band) ?: 0
    
    override fun getPresetCount(): Short = equalizer?.numberOfPresets ?: 0
    
    override fun getPresetName(preset: Short): String = equalizer?.getPresetName(preset) ?: ""
    
    override fun usePreset(preset: Short) {
        equalizer?.usePreset(preset)
    }

    // --- Bass Boost ---
    
    override fun setBassBoostEnabled(enabled: Boolean) {
        bassBoost?.enabled = enabled
    }

    override fun isBassBoostEnabled(): Boolean = bassBoost?.enabled ?: false

    override fun isBassBoostSupported(): Boolean = bassBoost?.strengthSupported ?: false

    override fun setBassBoostStrength(strength: Short) {
        // Strength ranges from 0 to 1000
        bassBoost?.setStrength(strength)
    }

    override fun getBassBoostStrength(): Short = bassBoost?.roundedStrength ?: 0

    // --- Virtualizer ---

    override fun setVirtualizerEnabled(enabled: Boolean) {
        virtualizer?.enabled = enabled
    }

    override fun isVirtualizerEnabled(): Boolean = virtualizer?.enabled ?: false
    
    override fun isVirtualizerSupported(): Boolean = virtualizer?.strengthSupported ?: false

    override fun setVirtualizerStrength(strength: Short) {
        // Strength ranges from 0 to 1000
        virtualizer?.setStrength(strength)
    }

    override fun getVirtualizerStrength(): Short = virtualizer?.roundedStrength ?: 0

    // --- Loudness Enhancer ---

    override fun setLoudnessEnabled(enabled: Boolean) {
        loudnessEnhancer?.enabled = enabled
    }

    override fun isLoudnessEnabled(): Boolean = loudnessEnhancer?.enabled ?: false
    
    override fun isLoudnessSupported(): Boolean = true // LoudnessEnhancer is software based, generally supported

    override fun setLoudnessGain(gainMb: Int) {
        loudnessEnhancer?.setTargetGain(gainMb)
    }

    override fun getLoudnessGain(): Int = loudnessEnhancer?.targetGain?.toInt() ?: 0
}
