package com.gemini.music.domain.usecase.sleeptimer

import com.gemini.music.domain.repository.MusicController
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * SetSleepTimerUseCase 單元測試
 * 
 * 測試策略：驗證正確呼叫 MusicController 的睡眠計時器設定方法
 */
class SetSleepTimerUseCaseTest {

    private lateinit var musicController: MusicController
    private lateinit var setSleepTimerUseCase: SetSleepTimerUseCase

    @BeforeEach
    fun setUp() {
        musicController = mockk(relaxed = true)
        setSleepTimerUseCase = SetSleepTimerUseCase(musicController)
    }

    @Nested
    @DisplayName("當設定睡眠計時器時")
    inner class WhenSettingSleepTimer {

        @Test
        @DisplayName("應該呼叫 setSleepTimer 並傳遞正確的分鐘數")
        fun `should call setSleepTimer with correct minutes`() {
            // Given
            val minutes = 30

            // When
            setSleepTimerUseCase(minutes)

            // Then
            verify(exactly = 1) { musicController.setSleepTimer(30) }
        }

        @Test
        @DisplayName("應該能處理不同的時間設定")
        fun `should handle various time settings`() {
            // Given
            val testCases = listOf(1, 15, 30, 60, 120)

            // When & Then
            testCases.forEach { minutes ->
                setSleepTimerUseCase(minutes)
                verify { musicController.setSleepTimer(minutes) }
            }
        }
    }

    @Nested
    @DisplayName("邊界情況")
    inner class EdgeCases {

        @Test
        @DisplayName("當傳入 0 分鐘時應該仍然呼叫 setSleepTimer")
        fun `should call setSleepTimer with zero minutes`() {
            // Given
            val minutes = 0

            // When
            setSleepTimerUseCase(minutes)

            // Then
            verify(exactly = 1) { musicController.setSleepTimer(0) }
        }

        @Test
        @DisplayName("當傳入負數時應該仍然傳遞給 controller")
        fun `should pass negative values to controller`() {
            // Given - 業務邏輯驗證應在其他層處理
            val minutes = -10

            // When
            setSleepTimerUseCase(minutes)

            // Then
            verify(exactly = 1) { musicController.setSleepTimer(-10) }
        }
    }
}
