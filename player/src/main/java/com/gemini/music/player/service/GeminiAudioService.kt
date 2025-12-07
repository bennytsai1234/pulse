package com.gemini.music.player.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * SIGMA Music 的核心播放服務。
 * 負責管理 ExoPlayer 生命週期、MediaSession 以及背景播放邏輯。
 */
@AndroidEntryPoint
class GeminiAudioService : MediaLibraryService() {

    // 透過 Hilt 注入已經配置好的 ExoPlayer 實例
    // 我們會在 DI Module 中設定 Gapless 播放與 Audio Focus
    @Inject
    lateinit var player: ExoPlayer

    private var mediaLibrarySession: MediaLibrarySession? = null

    private val librarySessionCallback = object : MediaLibrarySession.Callback {
        // 這裡處理來自 UI (MediaController) 或 Android Auto/Wear OS 的指令
        // 例如：連接請求、瀏覽媒體庫等
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands = connectionResult.availableSessionCommands
                .buildUpon()
                // 在此添加自定義指令 (例如：收藏歌曲、載入歌詞)
                .build()
            return MediaSession.ConnectionResult.accept(sessionCommands, connectionResult.availablePlayerCommands)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // 建立 Activity PendingIntent，點擊通知欄時跳轉回 App
        val openActivityIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName) ?: Intent(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 初始化 MediaLibrarySession
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, librarySessionCallback)
            .setSessionActivity(openActivityIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // 當使用者從「最近任務」將 App 滑掉時，確保播放器如果沒在播放就停止 Service
        val player = mediaLibrarySession?.player
        if (player == null || !player.playWhenReady || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }
}
