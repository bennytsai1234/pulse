package com.pulse.music.domain.usecase.crossfade

import com.pulse.music.domain.model.CrossfadeCurve
import com.pulse.music.domain.model.CrossfadeSettings
import com.pulse.music.domain.repository.CrossfadeSettingsRepository
import javax.inject.Inject

/**
 * 更新交叉淡入淡出設定的 Use Case
 */
class UpdateCrossfadeSettingsUseCase @Inject constructor(
    private val repository: CrossfadeSettingsRepository
) {
    /**
     * 更新完整設定
     */
    suspend operator fun invoke(settings: CrossfadeSettings) {
        repository.updateSettings(settings)
    }

    /**
     * 設定是否啟用交叉淡入淡出
     */
    suspend fun setEnabled(enabled: Boolean) {
        repository.setEnabled(enabled)
    }

    /**
     * 設定淡入淡出時長 (毫秒)
     */
    suspend fun setDurationMs(durationMs: Int) {
        val validDuration = durationMs.coerceIn(
            CrossfadeSettings.MIN_DURATION_MS,
            CrossfadeSettings.MAX_DURATION_MS
        )
        repository.setDurationMs(validDuration)
    }

    /**
     * 設定淡入淡出時長 (秒)
     */
    suspend fun setDurationSeconds(seconds: Int) {
        setDurationMs(seconds * 1000)
    }

    /**
     * 設定淡入淡出曲線
     */
    suspend fun setCurve(curve: CrossfadeCurve) {
        repository.setCurve(curve)
    }

    /**
     * 設定手動跳轉時是否套用
     */
    suspend fun setApplyOnManualSkip(apply: Boolean) {
        repository.setApplyOnManualSkip(apply)
    }

    /**
     * 設定專輯連續模式
     */
    suspend fun setAlbumContinuous(enabled: Boolean) {
        repository.setAlbumContinuous(enabled)
    }

    /**
     * 設定智慧靜音偵測
     */
    suspend fun setSilenceDetection(enabled: Boolean) {
        repository.setSilenceDetection(enabled)
    }
}
