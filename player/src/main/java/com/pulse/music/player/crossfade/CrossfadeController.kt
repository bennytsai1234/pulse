package com.pulse.music.player.crossfade

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pulse.music.domain.model.CrossfadeSettings
import com.pulse.music.domain.repository.CrossfadeSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交叉淡入淡出狀態
 */
sealed class CrossfadeState {
    /** 閒置狀態 (無交叉淡入淡出進行中) */
    object Idle : CrossfadeState()

    /** 正在進行交叉淡入淡出 */
    data class Crossfading(
        val progress: Float,
        val fadeOutVolume: Float,
        val fadeInVolume: Float
    ) : CrossfadeState()

    /** 交叉淡入淡出完成 */
    object Completed : CrossfadeState()

    /** 已禁用 */
    object Disabled : CrossfadeState()
}

/**
 * 交叉淡入淡出控制器
 *
 * 負責協調雙播放器的交叉淡入淡出邏輯，包括：
 * - 監控播放進度
 * - 在適當時機觸發交叉淡入淡出
 * - 管理音量動畫
 * - 處理播放器切換
 */
@Singleton
class CrossfadeController @Inject constructor(
    private val dualPlayerManager: DualPlayerManager,
    private val volumeAnimator: VolumeAnimator,
    private val settingsRepository: CrossfadeSettingsRepository
) {
    companion object {
        private const val TAG = "CrossfadeController"

        /** 位置監控間隔 (毫秒) */
        private const val POSITION_CHECK_INTERVAL_MS = 100L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var positionMonitorJob: Job? = null
    private var crossfadeJob: Job? = null

    /** 當前設定 */
    private var currentSettings = CrossfadeSettings.DEFAULT

    /** 是否正在進行交叉淡入淡出 */
    private val _isCrossfading = MutableStateFlow(false)
    val isCrossfading: StateFlow<Boolean> = _isCrossfading.asStateFlow()

    /** 交叉淡入淡出狀態 */
    private val _state = MutableStateFlow<CrossfadeState>(CrossfadeState.Idle)
    val state: StateFlow<CrossfadeState> = _state.asStateFlow()

    /** 下一首歌曲 (預載用) */
    private var nextMediaItem: MediaItem? = null

    /** 主播放器參考 (用於非交叉淡入淡出時的正常播放) */
    private var mainPlayer: ExoPlayer? = null

    init {
        // 監聽設定變更
        scope.launch {
            settingsRepository.crossfadeSettings.collect { settings ->
                currentSettings = settings
                if (!settings.enabled) {
                    stopCrossfade()
                    _state.value = CrossfadeState.Disabled
                } else {
                    _state.value = CrossfadeState.Idle
                }
            }
        }
    }

    /**
     * 設定主播放器參考
     */
    fun setMainPlayer(player: ExoPlayer) {
        mainPlayer = player
    }

    /**
     * 設定下一首要播放的歌曲
     */
    fun setNextMediaItem(mediaItem: MediaItem?) {
        nextMediaItem = mediaItem
    }

    /**
     * 開始監控播放進度
     * 在歌曲播放時調用，用於判斷何時開始交叉淡入淡出
     */
    fun startPositionMonitor(player: ExoPlayer, onCrossfadeTrigger: () -> Unit) {
        stopPositionMonitor()

        if (!currentSettings.enabled) return

        positionMonitorJob = scope.launch {
            while (isActive) {
                if (player.isPlaying && player.duration > 0) {
                    val currentPosition = player.currentPosition
                    val duration = player.duration
                    val crossfadeDuration = currentSettings.durationMs

                    // 計算觸發點
                    val triggerPosition = duration - crossfadeDuration

                    // 確保歌曲足夠長才觸發交叉淡入淡出
                    if (triggerPosition > 0 && currentPosition >= triggerPosition) {
                        // 檢查是否有下一首歌
                        val hasNext = player.hasNextMediaItem() || nextMediaItem != null

                        if (hasNext && !_isCrossfading.value) {
                            Log.d(TAG, "Triggering crossfade at position $currentPosition / $duration")
                            onCrossfadeTrigger()
                        }
                    }
                }
                delay(POSITION_CHECK_INTERVAL_MS)
            }
        }
    }

    /**
     * 停止位置監控
     */
    fun stopPositionMonitor() {
        positionMonitorJob?.cancel()
        positionMonitorJob = null
    }

    /**
     * 執行交叉淡入淡出
     *
     * @param currentPlayer 當前正在播放的播放器
     * @param nextMediaItem 下一首歌曲
     * @param onComplete 完成回調
     */
    fun performCrossfade(
        currentPlayer: ExoPlayer,
        nextMediaItem: MediaItem,
        onComplete: () -> Unit
    ) {
        if (_isCrossfading.value) {
            Log.w(TAG, "Crossfade already in progress")
            return
        }

        if (!currentSettings.enabled) {
            Log.d(TAG, "Crossfade disabled, skipping")
            onComplete()
            return
        }

        // 檢查記憶體
        if (!dualPlayerManager.hasEnoughMemory()) {
            Log.w(TAG, "Not enough memory for crossfade, falling back to direct transition")
            onComplete()
            return
        }

        _isCrossfading.value = true
        stopPositionMonitor()

        crossfadeJob = scope.launch {
            try {
                Log.d(TAG, "Starting crossfade with duration ${currentSettings.durationMs}ms")

                // 初始化雙播放器 (如果尚未初始化)
                if (!dualPlayerManager.isInitialized) {
                    dualPlayerManager.initialize()
                }

                // 在待機播放器上準備下一首歌
                dualPlayerManager.prepareNextTrack(nextMediaItem)

                // 等待一下讓待機播放器準備好
                delay(200)

                // 開始待機播放器
                dualPlayerManager.startStandbyPlayer()

                // 同時進行淡出和淡入動畫
                val fadeOutJob = volumeAnimator.fadeOut(
                    scope = this,
                    durationMs = currentSettings.durationMs.toLong(),
                    curve = currentSettings.curve,
                    startVolume = currentPlayer.volume,
                    onVolumeChange = { volume ->
                        currentPlayer.volume = volume
                        updateState(
                            fadeOutVolume = volume,
                            fadeInVolume = dualPlayerManager.standbyPlayer?.volume ?: 0f
                        )
                    }
                )

                val fadeInJob = volumeAnimator.fadeIn(
                    scope = this,
                    durationMs = currentSettings.durationMs.toLong(),
                    curve = currentSettings.curve,
                    targetVolume = 1f,
                    onVolumeChange = { volume ->
                        dualPlayerManager.setStandbyVolume(volume)
                    }
                )

                // 等待兩個動畫都完成
                fadeOutJob.join()
                fadeInJob.join()

                // 交換播放器
                dualPlayerManager.swapPlayers()

                // 停止舊的播放器
                currentPlayer.pause()
                currentPlayer.volume = 1f // 恢復音量

                _state.value = CrossfadeState.Completed

                Log.d(TAG, "Crossfade completed")
                onComplete()

            } catch (e: Exception) {
                Log.e(TAG, "Crossfade error", e)
                // 發生錯誤時恢復正常狀態
                currentPlayer.volume = 1f
                onComplete()
            } finally {
                _isCrossfading.value = false
                _state.value = CrossfadeState.Idle
            }
        }
    }

    /**
     * 停止交叉淡入淡出
     */
    fun stopCrossfade() {
        crossfadeJob?.cancel()
        crossfadeJob = null
        volumeAnimator.cancelAll()
        _isCrossfading.value = false
        _state.value = CrossfadeState.Idle

        // 恢復音量
        mainPlayer?.volume = 1f
        dualPlayerManager.activePlayer?.volume = 1f
        dualPlayerManager.resetStandbyPlayer()
    }

    /**
     * 處理手動跳轉
     * 如果設定中啟用了手動跳轉時的交叉淡入淡出，則執行淡入淡出
     */
    suspend fun handleManualSkip(
        player: ExoPlayer,
        nextMediaItem: MediaItem,
        onComplete: () -> Unit
    ) {
        if (!currentSettings.enabled || !currentSettings.applyOnManualSkip) {
            onComplete()
            return
        }

        performCrossfade(player, nextMediaItem, onComplete)
    }

    /**
     * 檢查是否應該跳過交叉淡入淡出 (專輯連續模式)
     */
    suspend fun shouldSkipCrossfade(currentAlbum: String?, nextAlbum: String?): Boolean {
        if (!currentSettings.albumContinuous) return false

        // 如果兩首歌是同一張專輯，則跳過交叉淡入淡出
        return currentAlbum != null && nextAlbum != null && currentAlbum == nextAlbum
    }

    private fun updateState(fadeOutVolume: Float, fadeInVolume: Float) {
        val progress = fadeInVolume // 使用淡入音量作為進度指標
        _state.value = CrossfadeState.Crossfading(
            progress = progress,
            fadeOutVolume = fadeOutVolume,
            fadeInVolume = fadeInVolume
        )
    }

    /**
     * 釋放資源
     */
    fun release() {
        stopCrossfade()
        stopPositionMonitor()
        dualPlayerManager.release()
        scope.cancel()
    }
}
