package com.pulse.music.domain.repository

import com.pulse.music.domain.model.CrossfadeCurve
import com.pulse.music.domain.model.CrossfadeSettings
import kotlinx.coroutines.flow.Flow

/**
 * 交叉淡入淡出設定 Repository 介面
 */
interface CrossfadeSettingsRepository {

    /**
     * 取得完整的交叉淡入淡出設定
     */
    val crossfadeSettings: Flow<CrossfadeSettings>

    /**
     * 是否啟用交叉淡入淡出
     */
    val enabled: Flow<Boolean>

    /**
     * 淡入淡出時長 (毫秒)
     */
    val durationMs: Flow<Int>

    /**
     * 淡入淡出曲線類型
     */
    val curve: Flow<CrossfadeCurve>

    /**
     * 手動跳轉時是否套用
     */
    val applyOnManualSkip: Flow<Boolean>

    /**
     * 專輯連續模式
     */
    val albumContinuous: Flow<Boolean>

    /**
     * 智慧靜音偵測
     */
    val silenceDetection: Flow<Boolean>

    // ==================== Setters ====================

    /**
     * 設定是否啟用交叉淡入淡出
     */
    suspend fun setEnabled(enabled: Boolean)

    /**
     * 設定淡入淡出時長 (毫秒)
     */
    suspend fun setDurationMs(durationMs: Int)

    /**
     * 設定淡入淡出曲線
     */
    suspend fun setCurve(curve: CrossfadeCurve)

    /**
     * 設定手動跳轉時是否套用
     */
    suspend fun setApplyOnManualSkip(apply: Boolean)

    /**
     * 設定專輯連續模式
     */
    suspend fun setAlbumContinuous(enabled: Boolean)

    /**
     * 設定智慧靜音偵測
     */
    suspend fun setSilenceDetection(enabled: Boolean)

    /**
     * 更新完整設定
     */
    suspend fun updateSettings(settings: CrossfadeSettings)
}
