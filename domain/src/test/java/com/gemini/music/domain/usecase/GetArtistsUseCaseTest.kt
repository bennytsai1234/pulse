package com.gemini.music.domain.usecase

import app.cash.turbine.test
import com.gemini.music.domain.model.Artist
import com.gemini.music.domain.repository.MusicRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * GetArtistsUseCase 單元測試
 * 
 * 測試策略：驗證 UseCase 正確轉發藝人資料
 */
class GetArtistsUseCaseTest {

    private lateinit var musicRepository: MusicRepository
    private lateinit var getArtistsUseCase: GetArtistsUseCase

    @BeforeEach
    fun setUp() {
        musicRepository = mockk()
        getArtistsUseCase = GetArtistsUseCase(musicRepository)
    }

    // ===== 測試用假資料 =====
    private fun createTestArtist(
        name: String = "Test Artist",
        songCount: Int = 10
    ) = Artist(
        name = name,
        songCount = songCount
    )

    @Nested
    @DisplayName("當 Repository 回傳藝人列表時")
    inner class WhenRepositoryReturnsArtists {

        @Test
        @DisplayName("應該正確轉發藝人列表")
        fun `should return artists from repository`() = runTest {
            // Given
            val expectedArtists = listOf(
                createTestArtist("Artist A", 25),
                createTestArtist("Artist B", 15),
                createTestArtist("Artist C", 30)
            )
            every { musicRepository.getArtists() } returns flowOf(expectedArtists)

            // When & Then
            getArtistsUseCase().test {
                val result = awaitItem()
                assertEquals(3, result.size)
                assertEquals(expectedArtists, result)
                awaitComplete()
            }
        }

        @Test
        @DisplayName("應該正確傳遞藝人屬性")
        fun `should preserve artist properties`() = runTest {
            // Given
            val testArtist = createTestArtist(
                name = "周杰倫",
                songCount = 100
            )
            every { musicRepository.getArtists() } returns flowOf(listOf(testArtist))

            // When & Then
            getArtistsUseCase().test {
                val result = awaitItem()
                assertEquals(1, result.size)
                with(result.first()) {
                    assertEquals("周杰倫", name)
                    assertEquals(100, songCount)
                }
                awaitComplete()
            }
        }
    }

    @Nested
    @DisplayName("當 Repository 回傳空列表時")
    inner class WhenRepositoryReturnsEmpty {

        @Test
        @DisplayName("應該回傳空列表")
        fun `should return empty list`() = runTest {
            // Given
            every { musicRepository.getArtists() } returns flowOf(emptyList())

            // When & Then
            getArtistsUseCase().test {
                val result = awaitItem()
                assertEquals(0, result.size)
                awaitComplete()
            }
        }
    }

    @Nested
    @DisplayName("當 Repository 更新資料時")
    inner class WhenRepositoryUpdates {

        @Test
        @DisplayName("應該發射所有更新")
        fun `should emit all updates`() = runTest {
            // Given
            val initial = listOf(createTestArtist("Artist A", 10))
            val updated = listOf(
                createTestArtist("Artist A", 10),
                createTestArtist("Artist B", 5)
            )
            every { musicRepository.getArtists() } returns flowOf(initial, updated)

            // When & Then
            getArtistsUseCase().test {
                assertEquals(1, awaitItem().size)
                assertEquals(2, awaitItem().size)
                awaitComplete()
            }
        }
    }
}
