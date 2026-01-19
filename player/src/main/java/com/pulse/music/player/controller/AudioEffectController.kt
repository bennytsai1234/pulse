package com.pulse.music.player.controller

import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEffectController @Inject constructor() {

    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    fun init(audioSessionId: Int) {
        release()
        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = false
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        bassBoost?.release()
        bassBoost = null
        virtualizer?.release()
        virtualizer = null
    }

    // --- Bass Boost ---
    
    fun setBassBoostEnabled(enabled: Boolean) {
        bassBoost?.enabled = enabled
    }

    fun isBassBoostEnabled(): Boolean = bassBoost?.enabled ?: false

    fun isBassBoostSupported(): Boolean = bassBoost?.strengthSupported ?: false

    fun setBassBoostStrength(strength: Short) {
        // Strength ranges from 0 to 1000
        bassBoost?.setStrength(strength)
    }

    fun getBassBoostStrength(): Short = bassBoost?.roundedStrength ?: 0

    // --- Virtualizer ---

    fun setVirtualizerEnabled(enabled: Boolean) {
        virtualizer?.enabled = enabled
    }

    fun isVirtualizerEnabled(): Boolean = virtualizer?.enabled ?: false
    
    fun isVirtualizerSupported(): Boolean = virtualizer?.strengthSupported ?: false

    fun setVirtualizerStrength(strength: Short) {
        // Strength ranges from 0 to 1000
        virtualizer?.setStrength(strength)
    }

    fun getVirtualizerStrength(): Short = virtualizer?.roundedStrength ?: 0
}
