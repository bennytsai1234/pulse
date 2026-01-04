package com.pulse.music.player.crossfade

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 雙播放器管理器
 * 管理兩個 ExoPlayer 實例，實現交叉淡入淡出播放
 */
@Singleton
class DualPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var primaryPlayer: ExoPlayer? = null
    private var secondaryPlayer: ExoPlayer? = null

    /** 當前活躍的播放器 (正在播放的) */
    private var activePlayerIndex = 0

    /** 是否已初始化 */
    val isInitialized: Boolean
        get() = primaryPlayer != null && secondaryPlayer != null

    /**
     * 取得當前活躍的播放器
     */
    val activePlayer: ExoPlayer?
        get() = if (activePlayerIndex == 0) primaryPlayer else secondaryPlayer

    /**
     * 取得等待中的播放器 (用於預載下一首)
     */
    val standbyPlayer: ExoPlayer?
        get() = if (activePlayerIndex == 0) secondaryPlayer else primaryPlayer

    /**
     * 初始化雙播放器
     * 建立兩個獨立的 ExoPlayer 實例
     */
    fun initialize() {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        primaryPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, false) // 不自動處理 Audio Focus (由主播放器處理)
            .setHandleAudioBecomingNoisy(false)
            .build()

        secondaryPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, false)
            .setHandleAudioBecomingNoisy(false)
            .build()
    }

    /**
     * 在等待播放器上準備下一首歌曲
     *
     * @param mediaItem 要預載的媒體項目
     * @param startPositionMs 起始位置 (毫秒)
     */
    fun prepareNextTrack(mediaItem: MediaItem, startPositionMs: Long = 0L) {
        standbyPlayer?.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            volume = 0f // 初始音量為 0
            seekTo(startPositionMs)
            prepare()
        }
    }

    /**
     * 開始待機播放器的播放 (用於交叉淡入淡出)
     */
    fun startStandbyPlayer() {
        standbyPlayer?.play()
    }

    /**
     * 交換活躍與待機播放器
     * 在交叉淡入淡出完成後調用
     */
    fun swapPlayers() {
        activePlayerIndex = if (activePlayerIndex == 0) 1 else 0
    }

    /**
     * 停止並重置待機播放器
     */
    fun resetStandbyPlayer() {
        standbyPlayer?.apply {
            stop()
            clearMediaItems()
            volume = 0f
        }
    }

    /**
     * 設定活躍播放器音量
     */
    fun setActiveVolume(volume: Float) {
        activePlayer?.volume = volume.coerceIn(0f, 1f)
    }

    /**
     * 設定待機播放器音量
     */
    fun setStandbyVolume(volume: Float) {
        standbyPlayer?.volume = volume.coerceIn(0f, 1f)
    }

    /**
     * 新增監聽器到活躍播放器
     */
    fun addActivePlayerListener(listener: Player.Listener) {
        activePlayer?.addListener(listener)
    }

    /**
     * 移除監聽器
     */
    fun removeActivePlayerListener(listener: Player.Listener) {
        activePlayer?.removeListener(listener)
    }

    /**
     * 釋放所有資源
     */
    fun release() {
        primaryPlayer?.release()
        secondaryPlayer?.release()
        primaryPlayer = null
        secondaryPlayer = null
        activePlayerIndex = 0
    }

    /**
     * 檢查是否有足夠記憶體來運行雙播放器
     * 如果記憶體不足，應降級為單播放器模式
     */
    fun hasEnoughMemory(): Boolean {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory

        // 需要至少 30MB 可用記憶體才啟用雙播放器
        return availableMemory > 30 * 1024 * 1024
    }
}
