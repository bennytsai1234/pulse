package com.pulse.music.domain.model

/**
 * 交叉淡入淡出曲線類型
 */
enum class CrossfadeCurve {
    /** 線性曲線: y = x */
    LINEAR,
    /** 指數曲線: y = x^2 */
    EXPONENTIAL,
    /** S 曲線 (smoothstep): y = 3x^2 - 2x^3 */
    S_CURVE;

    companion object {
        fun fromOrdinal(ordinal: Int): CrossfadeCurve =
            entries.getOrElse(ordinal) { LINEAR }
    }
}

/**
 * 交叉淡入淡出設定資料模型
 *
 * @property enabled 是否啟用交叉淡入淡出
 * @property durationMs 淡入淡出時長 (毫秒)
 * @property curve 淡入淡出曲線類型
 * @property applyOnManualSkip 手動跳轉時是否套用
 * @property albumContinuous 專輯連續模式 (同專輯歌曲使用無縫過渡)
 * @property silenceDetection 智慧靜音偵測
 * @property silenceThresholdDb 靜音偵測閾值 (dB)
 */
data class CrossfadeSettings(
    val enabled: Boolean = false,
    val durationMs: Int = DEFAULT_DURATION_MS,
    val curve: CrossfadeCurve = CrossfadeCurve.LINEAR,
    val applyOnManualSkip: Boolean = true,
    val albumContinuous: Boolean = true,
    val silenceDetection: Boolean = false,
    val silenceThresholdDb: Float = -45f
) {
    /**
     * 取得淡入淡出時長 (秒)
     */
    val durationSeconds: Int
        get() = durationMs / 1000

    /**
     * 根據進度計算音量值
     * @param progress 淡入淡出進度 (0.0 ~ 1.0)
     * @return 音量值 (0.0 ~ 1.0)
     */
    fun calculateVolume(progress: Float): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return when (curve) {
            CrossfadeCurve.LINEAR -> clampedProgress
            CrossfadeCurve.EXPONENTIAL -> clampedProgress * clampedProgress
            CrossfadeCurve.S_CURVE -> clampedProgress * clampedProgress * (3 - 2 * clampedProgress)
        }
    }

    companion object {
        const val MIN_DURATION_MS = 1000    // 1 秒
        const val MAX_DURATION_MS = 12000   // 12 秒
        const val DEFAULT_DURATION_MS = 5000 // 5 秒

        /** 預設設定 */
        val DEFAULT = CrossfadeSettings()

        /**
         * 從秒數建立設定
         */
        fun withDurationSeconds(seconds: Int): CrossfadeSettings =
            CrossfadeSettings(
                enabled = seconds > 0,
                durationMs = (seconds * 1000).coerceIn(MIN_DURATION_MS, MAX_DURATION_MS)
            )
    }
}
