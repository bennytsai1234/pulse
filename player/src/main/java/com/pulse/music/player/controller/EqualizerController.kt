package com.pulse.music.player.controller

import android.media.audiofx.Equalizer
import javax.inject.Inject
import javax.inject.Singleton
import androidx.media3.exoplayer.ExoPlayer

@Singleton
class EqualizerController @Inject constructor() {

    private var equalizer: Equalizer? = null

    fun init(audioSessionId: Int) {
        release()
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        equalizer?.release()
        equalizer = null
    }

    fun getNumberOfBands(): Short {
        return equalizer?.numberOfBands ?: 0
    }

    fun getBandLevelRange(): ShortArray? {
        return equalizer?.bandLevelRange
    }

    fun setBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }

    fun getBandLevel(band: Short): Short {
        return equalizer?.getBandLevel(band) ?: 0
    }

    fun getCenterFreq(band: Short): Int {
        return equalizer?.getCenterFreq(band) ?: 0
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
    }

    fun isEnabled(): Boolean {
        return equalizer?.enabled ?: false
    }
}
