package com.pulse.music.player.crossfade

import com.pulse.music.domain.model.CrossfadeCurve
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * VolumeAnimator 單元測試
 *
 * 測試策略：驗證曲線計算的正確性
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VolumeAnimatorTest {

    private lateinit var volumeAnimator: VolumeAnimator

    @BeforeEach
    fun setUp() {
        volumeAnimator = VolumeAnimator()
    }

    @Nested
    @DisplayName("線性曲線計算")
    inner class LinearCurveTests {

        @Test
        @DisplayName("進度 0.0 應返回音量 0.0")
        fun `progress 0 should return volume 0`() {
            val result = volumeAnimator.calculateVolume(0f, CrossfadeCurve.LINEAR)
            assertEquals(0f, result, 0.001f)
        }

        @Test
        @DisplayName("進度 0.5 應返回音量 0.5")
        fun `progress 0_5 should return volume 0_5`() {
            val result = volumeAnimator.calculateVolume(0.5f, CrossfadeCurve.LINEAR)
            assertEquals(0.5f, result, 0.001f)
        }

        @Test
        @DisplayName("進度 1.0 應返回音量 1.0")
        fun `progress 1 should return volume 1`() {
            val result = volumeAnimator.calculateVolume(1f, CrossfadeCurve.LINEAR)
            assertEquals(1f, result, 0.001f)
        }

        @Test
        @DisplayName("線性曲線應保持線性關係")
        fun `linear curve should maintain linear relationship`() {
            val progressValues = listOf(0.1f, 0.25f, 0.33f, 0.67f, 0.75f, 0.9f)
            for (progress in progressValues) {
                val result = volumeAnimator.calculateVolume(progress, CrossfadeCurve.LINEAR)
                assertEquals(progress, result, 0.001f, "進度 $progress 應返回 $progress")
            }
        }
    }

    @Nested
    @DisplayName("指數曲線計算")
    inner class ExponentialCurveTests {

        @Test
        @DisplayName("進度 0.0 應返回音量 0.0")
        fun `progress 0 should return volume 0`() {
            val result = volumeAnimator.calculateVolume(0f, CrossfadeCurve.EXPONENTIAL)
            assertEquals(0f, result, 0.001f)
        }

        @Test
        @DisplayName("進度 0.5 應返回音量 0.25 (指數效果)")
        fun `progress 0_5 should return volume 0_25`() {
            val result = volumeAnimator.calculateVolume(0.5f, CrossfadeCurve.EXPONENTIAL)
            assertEquals(0.25f, result, 0.001f) // 0.5 * 0.5 = 0.25
        }

        @Test
        @DisplayName("進度 1.0 應返回音量 1.0")
        fun `progress 1 should return volume 1`() {
            val result = volumeAnimator.calculateVolume(1f, CrossfadeCurve.EXPONENTIAL)
            assertEquals(1f, result, 0.001f)
        }

        @Test
        @DisplayName("指數曲線值應小於線性曲線值 (除了端點)")
        fun `exponential values should be less than linear except at endpoints`() {
            val progressValues = listOf(0.2f, 0.3f, 0.4f, 0.6f, 0.7f, 0.8f)
            for (progress in progressValues) {
                val exponential = volumeAnimator.calculateVolume(progress, CrossfadeCurve.EXPONENTIAL)
                assertTrue(exponential < progress, "指數值 $exponential 應小於線性值 $progress")
            }
        }
    }

    @Nested
    @DisplayName("S 曲線計算")
    inner class SCurveTests {

        @Test
        @DisplayName("進度 0.0 應返回音量 0.0")
        fun `progress 0 should return volume 0`() {
            val result = volumeAnimator.calculateVolume(0f, CrossfadeCurve.S_CURVE)
            assertEquals(0f, result, 0.001f)
        }

        @Test
        @DisplayName("進度 0.5 應返回音量 0.5 (S 曲線對稱)")
        fun `progress 0_5 should return volume 0_5`() {
            val result = volumeAnimator.calculateVolume(0.5f, CrossfadeCurve.S_CURVE)
            // Smoothstep at 0.5: 3 * 0.25 - 2 * 0.125 = 0.75 - 0.25 = 0.5
            assertEquals(0.5f, result, 0.001f)
        }

        @Test
        @DisplayName("進度 1.0 應返回音量 1.0")
        fun `progress 1 should return volume 1`() {
            val result = volumeAnimator.calculateVolume(1f, CrossfadeCurve.S_CURVE)
            assertEquals(1f, result, 0.001f)
        }

        @Test
        @DisplayName("S 曲線前半段應低於線性")
        fun `s_curve first half should be below linear`() {
            val progressValues = listOf(0.1f, 0.2f, 0.3f, 0.4f)
            for (progress in progressValues) {
                val sCurve = volumeAnimator.calculateVolume(progress, CrossfadeCurve.S_CURVE)
                assertTrue(sCurve < progress, "前半段 S 曲線值 $sCurve 應小於線性值 $progress")
            }
        }

        @Test
        @DisplayName("S 曲線後半段應高於線性")
        fun `s_curve second half should be above linear`() {
            val progressValues = listOf(0.6f, 0.7f, 0.8f, 0.9f)
            for (progress in progressValues) {
                val sCurve = volumeAnimator.calculateVolume(progress, CrossfadeCurve.S_CURVE)
                assertTrue(sCurve > progress, "後半段 S 曲線值 $sCurve 應大於線性值 $progress")
            }
        }
    }

    @Nested
    @DisplayName("邊界情況處理")
    inner class EdgeCases {

        @Test
        @DisplayName("負數進度應被 clamp 到 0")
        fun `negative progress should clamp to 0`() {
            val result = volumeAnimator.calculateVolume(-0.5f, CrossfadeCurve.LINEAR)
            assertEquals(0f, result, 0.001f)
        }

        @Test
        @DisplayName("超過 1 的進度應被 clamp 到 1")
        fun `progress greater than 1 should clamp to 1`() {
            val result = volumeAnimator.calculateVolume(1.5f, CrossfadeCurve.LINEAR)
            assertEquals(1f, result, 0.001f)
        }

        @Test
        @DisplayName("所有曲線類型應正確處理極端負值")
        fun `all curves should handle extreme negative values`() {
            for (curve in CrossfadeCurve.entries) {
                val result = volumeAnimator.calculateVolume(-100f, curve)
                assertEquals(0f, result, 0.001f, "曲線 $curve 應將 -100 clamp 到 0")
            }
        }

        @Test
        @DisplayName("所有曲線類型應正確處理極端正值")
        fun `all curves should handle extreme positive values`() {
            for (curve in CrossfadeCurve.entries) {
                val result = volumeAnimator.calculateVolume(100f, curve)
                assertEquals(1f, result, 0.001f, "曲線 $curve 應將 100 clamp 到 1")
            }
        }
    }

    @Nested
    @DisplayName("淡入淡出動畫")
    inner class FadeAnimationTests {

        private val testDispatcher = StandardTestDispatcher()
        private val testScope = TestScope(testDispatcher)

        @Test
        @DisplayName("淡出動畫應從起始音量降到 0")
        fun `fadeOut should decrease volume from start to 0`() = testScope.runTest {
            val volumeChanges = mutableListOf<Float>()

            val job = volumeAnimator.fadeOut(
                scope = this,
                durationMs = 1000L,
                curve = CrossfadeCurve.LINEAR,
                startVolume = 1f,
                onVolumeChange = { volumeChanges.add(it) }
            )

            advanceTimeBy(1500L) // 確保動畫完成
            job.join()

            assertTrue(volumeChanges.isNotEmpty(), "應有音量變化")
            assertEquals(0f, volumeChanges.last(), 0.001f, "最終音量應為 0")
            assertTrue(volumeChanges.first() > volumeChanges.last(), "音量應遞減")
        }

        @Test
        @DisplayName("淡入動畫應從 0 升到目標音量")
        fun `fadeIn should increase volume from 0 to target`() = testScope.runTest {
            val volumeChanges = mutableListOf<Float>()

            val job = volumeAnimator.fadeIn(
                scope = this,
                durationMs = 1000L,
                curve = CrossfadeCurve.LINEAR,
                targetVolume = 1f,
                onVolumeChange = { volumeChanges.add(it) }
            )

            advanceTimeBy(1500L) // 確保動畫完成
            job.join()

            assertTrue(volumeChanges.isNotEmpty(), "應有音量變化")
            assertEquals(0f, volumeChanges.first(), 0.001f, "初始音量應為 0")
            assertEquals(1f, volumeChanges.last(), 0.001f, "最終音量應為 1")
        }

        @Test
        @DisplayName("取消動畫後應停止音量變化")
        fun `cancel should stop volume changes`() = testScope.runTest {
            val volumeChanges = mutableListOf<Float>()

            volumeAnimator.fadeOut(
                scope = this,
                durationMs = 5000L, // 長動畫
                curve = CrossfadeCurve.LINEAR,
                startVolume = 1f,
                onVolumeChange = { volumeChanges.add(it) }
            )

            advanceTimeBy(500L) // 讓動畫進行一段時間
            val countBeforeCancel = volumeChanges.size

            volumeAnimator.cancelAll()

            advanceTimeBy(500L)
            val countAfterCancel = volumeChanges.size

            // 取消後應很快停止（可能再有少量更新）
            assertTrue(countAfterCancel <= countBeforeCancel + 2, "取消後音量變化應停止")
        }
    }
}
