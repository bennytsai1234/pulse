package com.gemini.music.domain.usecase

import app.cash.turbine.test
import com.gemini.music.domain.model.ScanStatus
import com.gemini.music.domain.repository.MusicRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * ScanLocalMusicUseCase 單元測試
 * 
 * 測試策略：驗證 UseCase 正確傳遞掃描狀態流
 */
class ScanLocalMusicUseCaseTest {

    private lateinit var musicRepository: MusicRepository
    private lateinit var scanLocalMusicUseCase: ScanLocalMusicUseCase

    @BeforeEach
    fun setUp() {
        musicRepository = mockk()
        scanLocalMusicUseCase = ScanLocalMusicUseCase(musicRepository)
    }

    @Nested
    @DisplayName("當掃描成功完成時")
    inner class WhenScanCompletes {

        @Test
        @DisplayName("應該依序發射 Scanning 和 Completed 狀態")
        fun `should emit scanning and completed status in order`() = runTest {
            // Given
            val scanningStatus = ScanStatus.Scanning(50, 100, "processing...")
            val completedStatus = ScanStatus.Completed(100)
            every { musicRepository.scanLocalMusic() } returns flowOf(scanningStatus, completedStatus)

            // When & Then
            scanLocalMusicUseCase().test {
                val first = awaitItem()
                assertTrue(first is ScanStatus.Scanning)
                assertEquals(50, (first as ScanStatus.Scanning).progress)

                val second = awaitItem()
                assertTrue(second is ScanStatus.Completed)
                assertEquals(100, (second as ScanStatus.Completed).totalAdded)

                awaitComplete()
            }
        }

        @Test
        @DisplayName("應該正確傳遞總數量")
        fun `should pass correct total count`() = runTest {
            // Given
            val completedStatus = ScanStatus.Completed(42)
            every { musicRepository.scanLocalMusic() } returns flowOf(completedStatus)

            // When & Then
            scanLocalMusicUseCase().test {
                val result = awaitItem()
                assertTrue(result is ScanStatus.Completed)
                assertEquals(42, (result as ScanStatus.Completed).totalAdded)
                awaitComplete()
            }
        }
    }

    @Nested
    @DisplayName("當掃描失敗時")
    inner class WhenScanFails {

        @Test
        @DisplayName("應該發射 Failed 狀態並包含錯誤訊息")
        fun `should emit failed status with error message`() = runTest {
            // Given
            val failedStatus = ScanStatus.Failed("Permission denied")
            every { musicRepository.scanLocalMusic() } returns flowOf(failedStatus)

            // When & Then
            scanLocalMusicUseCase().test {
                val result = awaitItem()
                assertTrue(result is ScanStatus.Failed)
                assertEquals("Permission denied", (result as ScanStatus.Failed).error)
                awaitComplete()
            }
        }
    }

    @Nested
    @DisplayName("掃描進度更新時")
    inner class WhenScanProgressUpdates {

        @Test
        @DisplayName("應該發射多個進度更新")
        fun `should emit multiple progress updates`() = runTest {
            // Given
            val progress1 = ScanStatus.Scanning(0, 100, "Starting...")
            val progress2 = ScanStatus.Scanning(50, 100, "Half way...")
            val progress3 = ScanStatus.Scanning(100, 100, "Almost done...")
            val completed = ScanStatus.Completed(100)
            
            every { musicRepository.scanLocalMusic() } returns 
                flowOf(progress1, progress2, progress3, completed)

            // When & Then
            scanLocalMusicUseCase().test {
                assertEquals(0, (awaitItem() as ScanStatus.Scanning).progress)
                assertEquals(50, (awaitItem() as ScanStatus.Scanning).progress)
                assertEquals(100, (awaitItem() as ScanStatus.Scanning).progress)
                assertTrue(awaitItem() is ScanStatus.Completed)
                awaitComplete()
            }
        }
    }
}
