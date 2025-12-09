package com.gemini.music.domain.usecase

import app.cash.turbine.test
import com.gemini.music.domain.model.Album
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
 * GetAlbumsUseCase 單元測試
 * 
 * 測試策略：驗證 UseCase 正確轉發專輯資料
 */
class GetAlbumsUseCaseTest {

    private lateinit var musicRepository: MusicRepository
    private lateinit var getAlbumsUseCase: GetAlbumsUseCase

    @BeforeEach
    fun setUp() {
        musicRepository = mockk()
        getAlbumsUseCase = GetAlbumsUseCase(musicRepository)
    }

    // ===== 測試用假資料 =====
    private fun createTestAlbum(
        id: Long = 1L,
        title: String = "Test Album",
        artist: String = "Test Artist",
        songCount: Int = 10
    ) = Album(
        id = id,
        title = title,
        artist = artist,
        songCount = songCount
    )

    @Nested
    @DisplayName("當 Repository 回傳專輯列表時")
    inner class WhenRepositoryReturnsAlbums {

        @Test
        @DisplayName("應該正確轉發專輯列表")
        fun `should return albums from repository`() = runTest {
            // Given
            val expectedAlbums = listOf(
                createTestAlbum(1, "Album 1", "Artist A", 12),
                createTestAlbum(2, "Album 2", "Artist B", 8),
                createTestAlbum(3, "Album 3", "Artist A", 15)
            )
            every { musicRepository.getAlbums() } returns flowOf(expectedAlbums)

            // When & Then
            getAlbumsUseCase().test {
                val result = awaitItem()
                assertEquals(3, result.size)
                assertEquals(expectedAlbums, result)
                awaitComplete()
            }
        }

        @Test
        @DisplayName("應該正確傳遞專輯屬性")
        fun `should preserve album properties`() = runTest {
            // Given
            val testAlbum = createTestAlbum(
                id = 42L,
                title = "特殊專輯",
                artist = "特殊藝人",
                songCount = 20
            )
            every { musicRepository.getAlbums() } returns flowOf(listOf(testAlbum))

            // When & Then
            getAlbumsUseCase().test {
                val result = awaitItem()
                assertEquals(1, result.size)
                with(result.first()) {
                    assertEquals(42L, id)
                    assertEquals("特殊專輯", title)
                    assertEquals("特殊藝人", artist)
                    assertEquals(20, songCount)
                    assertEquals("content://media/external/audio/albumart/42", artUri)
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
            every { musicRepository.getAlbums() } returns flowOf(emptyList())

            // When & Then
            getAlbumsUseCase().test {
                val result = awaitItem()
                assertEquals(0, result.size)
                awaitComplete()
            }
        }
    }
}
