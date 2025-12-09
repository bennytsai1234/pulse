package com.gemini.music.ui.home

import app.cash.turbine.test
import com.gemini.music.domain.model.ScanStatus
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
import com.gemini.music.domain.usecase.GetAlbumsUseCase
import com.gemini.music.domain.usecase.GetArtistsUseCase
import com.gemini.music.domain.usecase.GetRecentlyAddedSongsUseCase
import com.gemini.music.domain.usecase.GetSongsUseCase
import com.gemini.music.domain.usecase.PlaySongUseCase
import com.gemini.music.domain.usecase.ScanLocalMusicUseCase
import com.gemini.music.domain.usecase.ToggleShuffleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * HomeViewModel 單元測試
 * 
 * 測試策略：驗證 ViewModel 的 UI 狀態管理和業務邏輯
 * 使用 UnconfinedTestDispatcher 確保 Flow 立即執行
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    // 使用 UnconfinedTestDispatcher 讓 Flow 立即發射
    private val testDispatcher = UnconfinedTestDispatcher()

    // 使用 MutableStateFlow 以便在測試中動態更新
    private val songsFlow = MutableStateFlow<List<Song>>(emptyList())

    // Mocks
    private lateinit var getSongsUseCase: GetSongsUseCase
    private lateinit var getRecentlyAddedSongsUseCase: GetRecentlyAddedSongsUseCase
    private lateinit var getAlbumsUseCase: GetAlbumsUseCase
    private lateinit var getArtistsUseCase: GetArtistsUseCase
    private lateinit var scanLocalMusicUseCase: ScanLocalMusicUseCase
    private lateinit var playSongUseCase: PlaySongUseCase
    private lateinit var toggleShuffleUseCase: ToggleShuffleUseCase
    private lateinit var musicRepository: MusicRepository

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        getSongsUseCase = mockk()
        getRecentlyAddedSongsUseCase = mockk()
        getAlbumsUseCase = mockk()
        getArtistsUseCase = mockk()
        scanLocalMusicUseCase = mockk()
        playSongUseCase = mockk(relaxed = true)
        toggleShuffleUseCase = mockk(relaxed = true)
        musicRepository = mockk()

        // Default stubbing - 使用 MutableStateFlow 允許動態更新
        every { getSongsUseCase() } returns songsFlow
        every { getRecentlyAddedSongsUseCase() } returns MutableStateFlow(emptyList())
        every { getAlbumsUseCase() } returns MutableStateFlow(emptyList())
        every { getArtistsUseCase() } returns MutableStateFlow(emptyList())
        every { scanLocalMusicUseCase() } returns MutableStateFlow(ScanStatus.Completed(0))
        every { musicRepository.getPlaylists() } returns MutableStateFlow(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HomeViewModel(
        getSongsUseCase = getSongsUseCase,
        getRecentlyAddedSongsUseCase = getRecentlyAddedSongsUseCase,
        getAlbumsUseCase = getAlbumsUseCase,
        getArtistsUseCase = getArtistsUseCase,
        scanLocalMusicUseCase = scanLocalMusicUseCase,
        playSongUseCase = playSongUseCase,
        toggleShuffleUseCase = toggleShuffleUseCase,
        musicRepository = musicRepository
    )

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

    @Test
    fun `initial state should have isLoading true`() = runTest {
        // Given & When
        viewModel = createViewModel()

        // Then - 初始狀態應該是 isLoading = true
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `uiState should contain songs from getSongsUseCase`() = runTest {
        // Given
        val songs = listOf(
            createTestSong(1, "Song A"),
            createTestSong(2, "Song B")
        )
        songsFlow.value = songs
        
        // When
        viewModel = createViewModel()

        // Then - 使用 first() 取得當前狀態
        val state = viewModel.uiState.first { it.songs.isNotEmpty() }
        assertEquals(2, state.songs.size)
    }

    @Test
    fun `playSong should call playSongUseCase with correct parameters`() = runTest {
        // Given
        val songs = listOf(createTestSong(1, "Song 1"), createTestSong(2, "Song 2"))
        songsFlow.value = songs
        
        viewModel = createViewModel()
        
        // 等待狀態更新
        viewModel.uiState.first { it.songs.isNotEmpty() }

        // When
        viewModel.playSong(songs[1])

        // Then
        verify { playSongUseCase(songs, 1) }
    }

    @Test
    fun `shuffleAll should call playSongUseCase with shuffled list`() = runTest {
        // Given
        val songs = listOf(
            createTestSong(1, "Song 1"),
            createTestSong(2, "Song 2"),
            createTestSong(3, "Song 3")
        )
        songsFlow.value = songs
        
        viewModel = createViewModel()
        
        // 等待狀態更新
        viewModel.uiState.first { it.songs.isNotEmpty() }

        // When
        viewModel.shuffleAll()

        // Then - 驗證呼叫 playSongUseCase，startIndex = 0
        verify { playSongUseCase(any(), 0) }
    }

    @Test
    fun `enterSelectionMode should set isSelectionMode to true`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.enterSelectionMode()

        // Then
        val state = viewModel.uiState.first { it.isSelectionMode }
        assertTrue(state.isSelectionMode)
    }

    @Test
    fun `exitSelectionMode should clear selection and set isSelectionMode to false`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.enterSelectionMode()
        viewModel.toggleSongSelection(1)
        
        // 確認已進入選擇模式
        viewModel.uiState.first { it.isSelectionMode && it.selectedSongIds.isNotEmpty() }

        // When
        viewModel.exitSelectionMode()

        // Then
        val state = viewModel.uiState.first { !it.isSelectionMode }
        assertFalse(state.isSelectionMode)
        assertTrue(state.selectedSongIds.isEmpty())
    }

    @Test
    fun `toggleSongSelection should add songId to selectedSongIds`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.toggleSongSelection(42)

        // Then
        val state = viewModel.uiState.first { it.selectedSongIds.contains(42L) }
        assertTrue(state.selectedSongIds.contains(42L))
    }

    @Test
    fun `toggleSongSelection twice should remove songId from selectedSongIds`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.toggleSongSelection(42)
        
        // 確認已加入
        viewModel.uiState.first { it.selectedSongIds.contains(42L) }

        // When
        viewModel.toggleSongSelection(42) // Toggle again

        // Then
        val state = viewModel.uiState.first { !it.selectedSongIds.contains(42L) }
        assertFalse(state.selectedSongIds.contains(42L))
    }

    @Test
    fun `selectAll should select all songs`() = runTest {
        // Given
        val songs = listOf(
            createTestSong(1, "Song 1"),
            createTestSong(2, "Song 2"),
            createTestSong(3, "Song 3")
        )
        songsFlow.value = songs
        
        viewModel = createViewModel()
        
        // 等待歌曲載入
        viewModel.uiState.first { it.songs.isNotEmpty() }

        // When
        viewModel.selectAll()

        // Then
        val state = viewModel.uiState.first { it.selectedSongIds.size == 3 }
        assertEquals(3, state.selectedSongIds.size)
        assertTrue(state.selectedSongIds.containsAll(setOf(1L, 2L, 3L)))
    }

    @Test
    fun `setSortOption should update sortOption in state`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.setSortOption(SortOption.ARTIST)

        // Then
        val state = viewModel.uiState.first { it.sortOption == SortOption.ARTIST }
        assertEquals(SortOption.ARTIST, state.sortOption)
    }

    @Test
    fun `songs should be sorted by title when sortOption is TITLE`() = runTest {
        // Given
        val songs = listOf(
            createTestSong(1, "Zebra"),
            createTestSong(2, "Apple"),
            createTestSong(3, "Mango")
        )
        songsFlow.value = songs
        
        viewModel = createViewModel()
        
        // 等待歌曲載入
        viewModel.uiState.first { it.songs.isNotEmpty() }

        // When - 預設排序就是 TITLE
        viewModel.setSortOption(SortOption.TITLE)

        // Then
        val state = viewModel.uiState.first { it.songs.size == 3 }
        assertEquals("Apple", state.songs[0].title)
        assertEquals("Mango", state.songs[1].title)
        assertEquals("Zebra", state.songs[2].title)
    }

    @Test
    fun `createPlaylist should call repository and add selected songs`() = runTest {
        // Given
        val songs = listOf(createTestSong(1, "Song 1"), createTestSong(2, "Song 2"))
        songsFlow.value = songs
        coEvery { musicRepository.createPlaylist(any()) } returns 100L
        coEvery { musicRepository.addSongToPlaylist(any(), any()) } returns Unit
        
        viewModel = createViewModel()
        
        // 等待歌曲載入
        viewModel.uiState.first { it.songs.isNotEmpty() }
        
        viewModel.toggleSongSelection(1)
        viewModel.toggleSongSelection(2)
        
        // 等待選擇完成
        viewModel.uiState.first { it.selectedSongIds.size == 2 }

        // When
        viewModel.createPlaylist("My Playlist")

        // Then
        coVerify(timeout = 1000) { musicRepository.createPlaylist("My Playlist") }
        coVerify(timeout = 1000) { musicRepository.addSongToPlaylist(100L, 1L) }
        coVerify(timeout = 1000) { musicRepository.addSongToPlaylist(100L, 2L) }
    }
}
