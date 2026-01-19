package com.pulse.music.player.controller

import android.media.audiofx.PresetReverb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReverbController @Inject constructor() {

    private var presetReverb: PresetReverb? = null

    fun init(audioSessionId: Int) {
        release()
        try {
            presetReverb = PresetReverb(0, audioSessionId).apply {
                enabled = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        presetReverb?.release()
        presetReverb = null
    }

    fun setEnabled(enabled: Boolean) {
        presetReverb?.enabled = enabled
    }

    fun setPreset(preset: Short) {
        try {
            presetReverb?.preset = preset
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper to get preset names could be here or in UI
}
