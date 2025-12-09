package com.gemini.music.domain.usecase.favorites

import com.gemini.music.domain.repository.MusicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * ToggleFavoriteUseCase 單元測試
 * 
 * 測試策略：驗證正確呼叫 Repository 的 toggleFavorite 方法
 */
class ToggleFavoriteUseCaseTest {

    private lateinit var repository: MusicRepository
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    }

    @Nested
    @DisplayName("當切換收藏狀態時")
    inner class WhenTogglingFavorite {

        @Test
        @DisplayName("應該呼叫 repository.toggleFavorite 並傳遞正確的 songId")
        fun `should call toggleFavorite with correct songId`() = runTest {
            // Given
            val songId = 42L

            // When
            toggleFavoriteUseCase(songId)

            // Then
            coVerify(exactly = 1) { repository.toggleFavorite(42L) }
        }

        @Test
        @DisplayName("應該能處理不同的 songId")
        fun `should handle different songIds`() = runTest {
            // Given
            val songIds = listOf(1L, 100L, 999L, Long.MAX_VALUE)

            // When & Then
            songIds.forEach { songId ->
                toggleFavoriteUseCase(songId)
                coVerify { repository.toggleFavorite(songId) }
            }
        }
    }

    @Nested
    @DisplayName("當 Repository 拋出例外時")
    inner class WhenRepositoryThrowsException {

        @Test
        @DisplayName("應該將例外向上傳遞")
        fun `should propagate exception`() = runTest {
            // Given
            val songId = 1L
            val expectedException = RuntimeException("Database error")
            coEvery { repository.toggleFavorite(songId) } throws expectedException

            // When & Then
            try {
                toggleFavoriteUseCase(songId)
                assert(false) { "應該拋出例外" }
            } catch (e: RuntimeException) {
                assert(e.message == "Database error")
            }
        }
    }
}
