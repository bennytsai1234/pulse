package com.pulse.music.player.receiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.media3.common.Player
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Handles Headset plug and Bluetooth connection events to auto-play/pause.
 */
class HeadsetReceiver(
    private val player: Player,
    private val onHeadsetConnected: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        when (intent.action) {
            Intent.ACTION_HEADSET_PLUG -> {
                val state = intent.getIntExtra("state", -1)
                if (state == 1) {
                    // Connected
                    onHeadsetConnected()
                } else if (state == 0) {
                    // Disconnected - ExoPlayer handles becoming noisy usually, but explicit pause is safe
                    if (player.isPlaying) player.pause()
                }
            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                onHeadsetConnected()
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                if (player.isPlaying) player.pause()
            }
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }
}
