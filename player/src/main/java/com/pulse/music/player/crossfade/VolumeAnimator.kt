package com.pulse.music.player.crossfade

import com.pulse.music.domain.model.CrossfadeCurve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 音量動畫控制器
 * 負責計算並執行平滑的音量淡入淡出效果
 */
@Singleton
class VolumeAnimator @Inject constructor() {

    private var fadeOutJob: Job? = null
    private var fadeInJob: Job? = null

    companion object {
        /** 動畫更新頻率 (毫秒) */
        private const val ANIMATION_STEP_MS = 50L

        /** 最小動畫步數 */
        private const val MIN_STEPS = 10
    }

    /**
     * 根據曲線類型計算音量值
     *
     * @param progress 淡入淡出進度 (0.0 ~ 1.0)
     * @param curve 曲線類型
     * @return 音量值 (0.0 ~ 1.0)
     */
    fun calculateVolume(progress: Float, curve: CrossfadeCurve): Float {
        val clampedProgress = progress.coerceIn(0f, 1f)
        return when (curve) {
            CrossfadeCurve.LINEAR -> clampedProgress
            CrossfadeCurve.EXPONENTIAL -> clampedProgress * clampedProgress
            CrossfadeCurve.S_CURVE -> {
                // Smoothstep: 3x² - 2x³
                clampedProgress * clampedProgress * (3 - 2 * clampedProgress)
            }
        }
    }

    /**
     * 執行淡出動畫
     *
     * @param scope Coroutine 作用域
     * @param durationMs 淡出時長 (毫秒)
     * @param curve 曲線類型
     * @param startVolume 起始音量 (預設 1.0)
     * @param onVolumeChange 音量變更回調
     * @param onComplete 完成回調
     */
    fun fadeOut(
        scope: CoroutineScope,
        durationMs: Long,
        curve: CrossfadeCurve,
        startVolume: Float = 1f,
        onVolumeChange: (Float) -> Unit,
        onComplete: () -> Unit = {}
    ): Job {
        fadeOutJob?.cancel()
        fadeOutJob = scope.launch {
            val steps = maxOf(MIN_STEPS, (durationMs / ANIMATION_STEP_MS).toInt())
            val stepDurationMs = durationMs / steps

            for (step in 0..steps) {
                if (!isActive) break

                val progress = step.toFloat() / steps
                // 淡出：從 startVolume 降到 0
                val volume = startVolume * (1f - calculateVolume(progress, curve))
                onVolumeChange(volume.coerceIn(0f, 1f))

                if (step < steps) {
                    delay(stepDurationMs)
                }
            }

            if (isActive) {
                onVolumeChange(0f)
                onComplete()
            }
        }
        return fadeOutJob!!
    }

    /**
     * 執行淡入動畫
     *
     * @param scope Coroutine 作用域
     * @param durationMs 淡入時長 (毫秒)
     * @param curve 曲線類型
     * @param targetVolume 目標音量 (預設 1.0)
     * @param onVolumeChange 音量變更回調
     * @param onComplete 完成回調
     */
    fun fadeIn(
        scope: CoroutineScope,
        durationMs: Long,
        curve: CrossfadeCurve,
        targetVolume: Float = 1f,
        onVolumeChange: (Float) -> Unit,
        onComplete: () -> Unit = {}
    ): Job {
        fadeInJob?.cancel()
        fadeInJob = scope.launch {
            val steps = maxOf(MIN_STEPS, (durationMs / ANIMATION_STEP_MS).toInt())
            val stepDurationMs = durationMs / steps

            // 設定初始音量為 0
            onVolumeChange(0f)

            for (step in 0..steps) {
                if (!isActive) break

                val progress = step.toFloat() / steps
                // 淡入：從 0 升到 targetVolume
                val volume = targetVolume * calculateVolume(progress, curve)
                onVolumeChange(volume.coerceIn(0f, 1f))

                if (step < steps) {
                    delay(stepDurationMs)
                }
            }

            if (isActive) {
                onVolumeChange(targetVolume)
                onComplete()
            }
        }
        return fadeInJob!!
    }

    /**
     * 取消所有進行中的動畫
     */
    fun cancelAll() {
        fadeOutJob?.cancel()
        fadeInJob?.cancel()
        fadeOutJob = null
        fadeInJob = null
    }

    /**
     * 取消淡出動畫
     */
    fun cancelFadeOut() {
        fadeOutJob?.cancel()
        fadeOutJob = null
    }

    /**
     * 取消淡入動畫
     */
    fun cancelFadeIn() {
        fadeInJob?.cancel()
        fadeInJob = null
    }
}
