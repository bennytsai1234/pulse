package com.gemini.music.ui.nowplaying

import com.gemini.music.domain.model.MusicState
import com.gemini.music.domain.model.RepeatMode
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.usecase.CycleRepeatModeUseCase
import com.gemini.music.domain.usecase.FormattedPlaybackState
import com.gemini.music.domain.usecase.GetFormattedPlaybackStateUseCase
import com.gemini.music.domain.usecase.GetLyricsUseCase
import com.gemini.music.domain.usecase.GetMusicStateUseCase
import com.gemini.music.domain.usecase.GetSongWaveformUseCase
import com.gemini.music.domain.usecase.PlayQueueItemUseCase
import com.gemini.music.domain.usecase.RemoveQueueItemUseCase
import com.gemini.music.domain.usecase.SeekToUseCase
import com.gemini.music.domain.usecase.SkipToNextUseCase
import com.gemini.music.domain.usecase.SkipToPreviousUseCase
import com.gemini.music.domain.usecase.TogglePlayPauseUseCase
import com.gemini.music.domain.usecase.ToggleShuffleUseCase
import com.gemini.music.domain.usecase.favorites.IsSongFavoriteUseCase
import com.gemini.music.domain.usecase.favorites.ToggleFavoriteUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * NowPlayingViewModel 單元測試
 * 
 * 測試策略：驗證播放控制事件處理和 UI 狀態更新
 * 使用 UnconfinedTestDispatcher 確保 Flow 立即執行
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val musicStateFlow = MutableStateFlow(MusicState())

    // Mocks
    private lateinit var getMusicStateUseCase: GetMusicStateUseCase
    private lateinit var getLyricsUseCase: GetLyricsUseCase
    private lateinit var getFormattedPlaybackStateUseCase: GetFormattedPlaybackStateUseCase
    private lateinit var togglePlayPauseUseCase: TogglePlayPauseUseCase
    private lateinit var seekToUseCase: SeekToUseCase
    private lateinit var skipToNextUseCase: SkipToNextUseCase
    private lateinit var skipToPreviousUseCase: SkipToPreviousUseCase
    private lateinit var toggleShuffleUseCase: ToggleShuffleUseCase
    private lateinit var cycleRepeatModeUseCase: CycleRepeatModeUseCase
    private lateinit var playQueueItemUseCase: PlayQueueItemUseCase
    private lateinit var removeQueueItemUseCase: RemoveQueueItemUseCase
    private lateinit var getSongWaveformUseCase: GetSongWaveformUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var isSongFavoriteUseCase: IsSongFavoriteUseCase

    private lateinit var viewModel: NowPlayingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        getMusicStateUseCase = mockk()
        getLyricsUseCase = mockk()
        getFormattedPlaybackStateUseCase = mockk()
        togglePlayPauseUseCase = mockk(relaxed = true)
        seekToUseCase = mockk(relaxed = true)
        skipToNextUseCase = mockk(relaxed = true)
        skipToPreviousUseCase = mockk(relaxed = true)
        toggleShuffleUseCase = mockk(relaxed = true)
        cycleRepeatModeUseCase = mockk(relaxed = true)
        playQueueItemUseCase = mockk(relaxed = true)
        removeQueueItemUseCase = mockk(relaxed = true)
        getSongWaveformUseCase = mockk()
        toggleFavoriteUseCase = mockk(relaxed = true)
        isSongFavoriteUseCase = mockk()

        // Default stubbing
        every { getMusicStateUseCase() } returns musicStateFlow
        coEvery { getLyricsUseCase(any()) } returns emptyList()
        every { getFormattedPlaybackStateUseCase(any()) } returns MutableStateFlow(FormattedPlaybackState())
        coEvery { getSongWaveformUseCase(any()) } returns listOf(0)
        every { isSongFavoriteUseCase(any()) } returns MutableStateFlow(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = NowPlayingViewModel(
        getMusicStateUseCase = getMusicStateUseCase,
        getLyricsUseCase = getLyricsUseCase,
        getFormattedPlaybackStateUseCase = getFormattedPlaybackStateUseCase,
        togglePlayPauseUseCase = togglePlayPauseUseCase,
        seekToUseCase = seekToUseCase,
        skipToNextUseCase = skipToNextUseCase,
        skipToPreviousUseCase = skipToPreviousUseCase,
        toggleShuffleUseCase = toggleShuffleUseCase,
        cycleRepeatModeUseCase = cycleRepeatModeUseCase,
        playQueueItemUseCase = playQueueItemUseCase,
        removeQueueItemUseCase = removeQueueItemUseCase,
        getSongWaveformUseCase = getSongWaveformUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase,
        isSongFavoriteUseCase = isSongFavoriteUseCase
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
    fun `initial state should have null song`() = runTest {
        // Given & When
        viewModel = createViewModel()

        // Then
        assertNull(viewModel.uiState.value.song)
    }

    @Test
    fun `uiState should reflect current song from MusicState`() = runTest {
        // Given
        val testSong = createTestSong(42, "Now Playing Song")
        musicStateFlow.value = MusicState(currentSong = testSong, isPlaying = true)
        
        // When
        viewModel = createViewModel()
        
        // Then - 使用 first 等待狀態更新
        val state = viewModel.uiState.first { it.song != null }
        assertEquals(testSong, state.song)
        assertTrue(state.isPlaying)
    }

    @Test
    fun `PlayPauseToggle event should call togglePlayPauseUseCase`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.PlayPauseToggle)

        // Then
        verify(exactly = 1) { togglePlayPauseUseCase() }
    }

    @Test
    fun `SkipNext event should call skipToNextUseCase`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.SkipNext)

        // Then
        verify(exactly = 1) { skipToNextUseCase() }
    }

    @Test
    fun `SkipPrevious event should call skipToPreviousUseCase`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.SkipPrevious)

        // Then
        verify(exactly = 1) { skipToPreviousUseCase() }
    }

    @Test
    fun `SeekTo event should call seekToUseCase with correct position`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.SeekTo(0.5f))

        // Then
        verify(exactly = 1) { seekToUseCase(0.5f) }
    }

    @Test
    fun `ToggleShuffle event should call toggleShuffleUseCase`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.ToggleShuffle)

        // Then
        verify(exactly = 1) { toggleShuffleUseCase() }
    }

    @Test
    fun `ToggleRepeat event should call cycleRepeatModeUseCase`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.ToggleRepeat)

        // Then
        verify(exactly = 1) { cycleRepeatModeUseCase() }
    }

    @Test
    fun `PlayQueueItem event should call playQueueItemUseCase with correct index`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.PlayQueueItem(3))

        // Then
        verify(exactly = 1) { playQueueItemUseCase(3) }
    }

    @Test
    fun `RemoveFromQueue event should call removeQueueItemUseCase with correct index`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.RemoveFromQueue(2))

        // Then
        verify(exactly = 1) { removeQueueItemUseCase(2) }
    }

    @Test
    fun `ToggleFavorite event should call toggleFavoriteUseCase when song exists`() = runTest {
        // Given
        val testSong = createTestSong(42, "Favorite Song")
        musicStateFlow.value = MusicState(currentSong = testSong)
        
        viewModel = createViewModel()
        
        // 等待歌曲載入
        viewModel.uiState.first { it.song != null }

        // When
        viewModel.onEvent(NowPlayingEvent.ToggleFavorite)

        // Then
        coVerify(timeout = 1000) { toggleFavoriteUseCase(42L) }
    }

    @Test
    fun `ToggleFavorite event should not call useCase when song is null`() = runTest {
        // Given - no current song
        musicStateFlow.value = MusicState(currentSong = null)
        
        viewModel = createViewModel()

        // When
        viewModel.onEvent(NowPlayingEvent.ToggleFavorite)

        // Then
        coVerify(exactly = 0) { toggleFavoriteUseCase(any()) }
    }

    @Test
    fun `uiState should reflect shuffle mode from MusicState`() = runTest {
        // Given
        musicStateFlow.value = MusicState(shuffleModeEnabled = true)
        
        // When
        viewModel = createViewModel()
        
        // Then
        val state = viewModel.uiState.first { it.shuffleModeEnabled }
        assertTrue(state.shuffleModeEnabled)
    }

    @Test
    fun `uiState should reflect repeat mode from MusicState`() = runTest {
        // Given
        musicStateFlow.value = MusicState(repeatMode = RepeatMode.ONE)
        
        // When
        viewModel = createViewModel()
        
        // Then
        val state = viewModel.uiState.first { it.repeatMode == RepeatMode.ONE }
        assertEquals(RepeatMode.ONE, state.repeatMode)
    }

    @Test
    fun `uiState should reflect queue from MusicState`() = runTest {
        // Given
        val queue = listOf(
            createTestSong(1, "Song 1"),
            createTestSong(2, "Song 2"),
            createTestSong(3, "Song 3")
        )
        musicStateFlow.value = MusicState(queue = queue)
        
        // When
        viewModel = createViewModel()
        
        // Then
        val state = viewModel.uiState.first { it.queue.isNotEmpty() }
        assertEquals(3, state.queue.size)
    }
}
