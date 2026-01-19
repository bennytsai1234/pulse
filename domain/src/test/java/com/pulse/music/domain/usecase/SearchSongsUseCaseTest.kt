package com.pulse.music.domain.usecase

import app.cash.turbine.test
import com.pulse.music.domain.model.Song
import com.pulse.music.domain.repository.MusicRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchSongsUseCaseTest {

    private lateinit var musicRepository: MusicRepository
    private lateinit var searchSongsUseCase: SearchSongsUseCase

    @BeforeEach
    fun setUp() {
        musicRepository = mockk()
        searchSongsUseCase = SearchSongsUseCase(musicRepository)
    }

    private fun createTestSong(id: Long, title: String) = Song(
        id = id,
        title = title,
        artist = "Artist",
        album = "Album",
        albumId = 1L,
        duration = 1000L,
        contentUri = "",
        dataPath = ""
    )

    @Test
    fun `should forward query to repository`() = runTest {
        // Given
        val query = "test"
        val expectedSongs = listOf(createTestSong(1, "Test Song"))
        every { musicRepository.searchSongs(query) } returns flowOf(expectedSongs)

        // When & Then
        searchSongsUseCase(query).test {
            assertEquals(expectedSongs, awaitItem())
            awaitComplete()
        }
    }
}
