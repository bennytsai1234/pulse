package com.pulse.music.player.service

import android.app.Notification
import android.content.Context
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.pulse.music.core.common.PlayerConstants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service for handling media downloads in the background.
 */
@AndroidEntryPoint
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PulseDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    PlayerConstants.DOWNLOAD_CHANNEL_ID,
    com.pulse.music.player.R.string.download_channel_name,
    com.pulse.music.player.R.string.download_channel_description
) {

    @Inject
    lateinit var injectedDownloadManager: DownloadManager

    override fun getDownloadManager(): DownloadManager {
        return injectedDownloadManager
    }

    override fun getScheduler(): Scheduler? {
        return null // PlatformScheduler can be added later if needed
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        return notificationHelper.buildProgressNotification(
            this,
            com.pulse.music.player.R.drawable.ic_download, // Ensure this drawable exists or use android default
            null,
            null,
            downloads,
            notMetRequirements
        )
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private val notificationHelper: DownloadNotificationHelper by lazy {
        DownloadNotificationHelper(this, PlayerConstants.DOWNLOAD_CHANNEL_ID)
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 2001
    }
}
