package com.gemini.music.domain.usecase

import app.cash.turbine.test
import com.gemini.music.domain.model.Song
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
 * GetSongsUseCase 單元測試
 * 
 * 測試策略：驗證 UseCase 正確轉發 Repository 的資料
 */
class GetSongsUseCaseTest {

    private lateinit var musicRepository: MusicRepository
    private lateinit var getSongsUseCase: GetSongsUseCase

    @BeforeEach
    fun setUp() {
        musicRepository = mockk()
        getSongsUseCase = GetSongsUseCase(musicRepository)
    }

    // ===== 測試用假資料 =====
    private fun createTestSong(
        id: Long = 1L,
        title: String = "Test Song",
        artist: String = "Test Artist",
        album: String = "Test Album"
    ) = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = id * 100,
        duration = 180_000L,
        contentUri = "content://media/external/audio/media/$id",
        dataPath = "/storage/emulated/0/Music/$title.mp3"
    )

    @Nested
    @DisplayName("當 Repository 回傳歌曲列表時")
    inner class WhenRepositoryReturnsSongs {

        @Test
        @DisplayName("應該正確轉發歌曲列表")
        fun `should return songs from repository`() = runTest {
            // Given
            val expectedSongs = listOf(
                createTestSong(1, "Song 1"),
                createTestSong(2, "Song 2"),
                createTestSong(3, "Song 3")
            )
            every { musicRepository.getSongs() } returns flowOf(expectedSongs)

            // When & Then
            getSongsUseCase().test {
                val result = awaitItem()
                assertEquals(expectedSongs.size, result.size)
                assertEquals(expectedSongs, result)
                awaitComplete()
            }
        }

        @Test
        @DisplayName("應該正確傳遞歌曲屬性")
        fun `should preserve song properties`() = runTest {
            // Given
            val testSong = createTestSong(
                id = 42L,
                title = "特殊歌曲",
                artist = "特殊藝人",
                album = "特殊專輯"
            )
            every { musicRepository.getSongs() } returns flowOf(listOf(testSong))

            // When & Then
            getSongsUseCase().test {
                val result = awaitItem()
                assertEquals(1, result.size)
                with(result.first()) {
                    assertEquals(42L, id)
                    assertEquals("特殊歌曲", title)
                    assertEquals("特殊藝人", artist)
                    assertEquals("特殊專輯", album)
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
            every { musicRepository.getSongs() } returns flowOf(emptyList())

            // When & Then
            getSongsUseCase().test {
                val result = awaitItem()
                assertEquals(0, result.size)
                awaitComplete()
            }
        }
    }

    @Nested
    @DisplayName("當 Repository 發出多次更新時")
    inner class WhenRepositoryEmitsMultipleUpdates {

        @Test
        @DisplayName("應該依序傳遞所有更新")
        fun `should emit all updates in order`() = runTest {
            // Given
            val initialSongs = listOf(createTestSong(1, "Song 1"))
            val updatedSongs = listOf(
                createTestSong(1, "Song 1"),
                createTestSong(2, "Song 2")
            )
            every { musicRepository.getSongs() } returns flowOf(initialSongs, updatedSongs)

            // When & Then
            getSongsUseCase().test {
                // 第一次發射
                val first = awaitItem()
                assertEquals(1, first.size)

                // 第二次發射
                val second = awaitItem()
                assertEquals(2, second.size)

                awaitComplete()
            }
        }
    }
}
