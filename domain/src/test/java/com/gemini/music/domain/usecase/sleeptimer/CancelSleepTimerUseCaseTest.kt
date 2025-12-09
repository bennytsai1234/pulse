package com.gemini.music.domain.usecase.sleeptimer

import com.gemini.music.domain.repository.MusicController
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * CancelSleepTimerUseCase 單元測試
 * 
 * 測試策略：驗證正確呼叫 MusicController 的取消睡眠計時器方法
 */
class CancelSleepTimerUseCaseTest {

    private lateinit var musicController: MusicController
    private lateinit var cancelSleepTimerUseCase: CancelSleepTimerUseCase

    @BeforeEach
    fun setUp() {
        musicController = mockk(relaxed = true)
        cancelSleepTimerUseCase = CancelSleepTimerUseCase(musicController)
    }

    @Test
    @DisplayName("當取消睡眠計時器時應該呼叫 cancelSleepTimer")
    fun `should call cancelSleepTimer`() {
        // When
        cancelSleepTimerUseCase()

        // Then
        verify(exactly = 1) { musicController.cancelSleepTimer() }
    }

    @Test
    @DisplayName("多次呼叫應該多次觸發 cancelSleepTimer")
    fun `should call cancelSleepTimer multiple times when invoked multiple times`() {
        // When
        cancelSleepTimerUseCase()
        cancelSleepTimerUseCase()
        cancelSleepTimerUseCase()

        // Then
        verify(exactly = 3) { musicController.cancelSleepTimer() }
    }
}
