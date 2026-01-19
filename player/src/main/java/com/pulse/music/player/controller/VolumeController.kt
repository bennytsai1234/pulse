package com.pulse.music.player.controller

import javax.inject.Inject
import javax.inject.Singleton
import androidx.media3.exoplayer.ExoPlayer

@Singleton
class VolumeController @Inject constructor() {
    
    private var player: ExoPlayer? = null

    fun setPlayer(player: ExoPlayer) {
        this.player = player
    }

    fun setVolume(volume: Float) {
        player?.volume = volume
    }

    /**
     * Set Left/Right balance.
     * Implementation relies on ExoPlayer's volume capability or custom AudioProcessor.
     */
    fun setBalance(balance: Float) {
        // Implementation pending ChannelMappingAudioProcessor integration.
    }
}
