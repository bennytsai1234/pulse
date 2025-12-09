package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicController
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * PlaySongUseCase 單元測試
 * 
 * 測試策略：驗證 UseCase 正確呼叫 MusicController 的播放方法
 */
class PlaySongUseCaseTest {

    private lateinit var musicController: MusicController
    private lateinit var playSongUseCase: PlaySongUseCase

    @BeforeEach
    fun setUp() {
        musicController = mockk(relaxed = true)
        playSongUseCase = PlaySongUseCase(musicController)
    }

    // ===== 測試用假資料 =====
    private fun createTestSong(id: Long = 1L, title: String = "Test Song") = Song(
        id = id,
        title = title,
        artist = "Test Artist",
        album = "Test Album",
        albumId = id * 100,
        duration = 180_000L,
        contentUri = "content://media/external/audio/media/$id",
        dataPath = "/storage/emulated/0/Music/$title.mp3"
    )

    @Nested
    @DisplayName("當播放單首歌曲時")
    inner class WhenPlayingSingleSong {

        @Test
        @DisplayName("應該呼叫 MusicController.playSongs 並傳遞正確參數")
        fun `should call playSongs with correct parameters`() {
            // Given
            val songs = listOf(createTestSong(1, "Song 1"))
            val startIndex = 0

            // When
            playSongUseCase(songs, startIndex)

            // Then
            verify(exactly = 1) { musicController.playSongs(songs, startIndex) }
        }
    }

    @Nested
    @DisplayName("當播放多首歌曲時")
    inner class WhenPlayingMultipleSongs {

        @Test
        @DisplayName("應該傳遞完整歌曲列表")
        fun `should pass complete song list`() {
            // Given
            val songs = listOf(
                createTestSong(1, "Song 1"),
                createTestSong(2, "Song 2"),
                createTestSong(3, "Song 3")
            )
            val startIndex = 0

            // When
            playSongUseCase(songs, startIndex)

            // Then
            verify(exactly = 1) { 
                musicController.playSongs(
                    match { it.size == 3 }, 
                    startIndex
                ) 
            }
        }

        @Test
        @DisplayName("從中間開始播放時應該傳遞正確的 startIndex")
        fun `should pass correct startIndex when starting from middle`() {
            // Given
            val songs = listOf(
                createTestSong(1, "Song 1"),
                createTestSong(2, "Song 2"),
                createTestSong(3, "Song 3")
            )
            val startIndex = 1 // 從第二首開始

            // When
            playSongUseCase(songs, startIndex)

            // Then
            verify(exactly = 1) { musicController.playSongs(songs, 1) }
        }
    }

    @Nested
    @DisplayName("當傳入空列表時")
    inner class WhenPassingEmptyList {

        @Test
        @DisplayName("應該仍然呼叫 MusicController")
        fun `should still call musicController`() {
            // Given
            val emptySongs = emptyList<Song>()

            // When
            playSongUseCase(emptySongs, 0)

            // Then
            verify(exactly = 1) { musicController.playSongs(emptySongs, 0) }
        }
    }
}
