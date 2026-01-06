package com.pulse.music.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.pulse.music.data.database.FavoriteDao
import com.pulse.music.data.database.FavoriteEntity
import com.pulse.music.data.database.PulseDatabase
import com.pulse.music.data.database.PlaylistDao
import com.pulse.music.data.database.SongDao
import com.pulse.music.data.database.SongEntity
import com.pulse.music.data.source.LocalAudioSource
import com.pulse.music.domain.model.ScanStatus
import com.pulse.music.domain.model.Song
import com.pulse.music.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * MusicRepositoryImpl 整合測試
 *
 * 使用真實的 Room 資料庫（記憶體模式）和 Mock 的外部依賴測試 Repository 邏輯
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MusicRepositoryImplTest {

    private lateinit var database: PulseDatabase
    private lateinit var songDao: SongDao
    private lateinit var playlistDao: PlaylistDao
    private lateinit var favoriteDao: FavoriteDao

    // Mocked dependencies
    private lateinit var localAudioSource: LocalAudioSource
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private lateinit var repository: MusicRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        songDao = database.songDao()
        playlistDao = database.playlistDao()
        favoriteDao = database.favoriteDao()

        // Mock external dependencies
        localAudioSource = mockk()
        userPreferencesRepository = mockk()
        val tagEditorSource = mockk<com.pulse.music.data.source.TagEditorSource>()

        // Default mock behavior
        every { userPreferencesRepository.minAudioDuration } returns MutableStateFlow(30_000L)
        every { userPreferencesRepository.includedFolders } returns MutableStateFlow(emptySet())

        repository = MusicRepositoryImpl(
            localAudioSource = localAudioSource,
            tagEditorSource = tagEditorSource,
            songDao = songDao,
            playlistDao = playlistDao,
            favoriteDao = favoriteDao,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ===== 測試用假資料 =====
    private fun createTestSong(id: Long, title: String, artist: String = "Test Artist", album: String = "Test Album") = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = id * 100,
        duration = 180_000L,
        contentUri = "content://media/external/audio/media/$id",
        dataPath = "/storage/emulated/0/Music/$title.mp3"
    )

    private fun createTestSongEntity(id: Long, title: String, artist: String = "Test Artist", album: String = "Test Album", albumId: Long = id * 100) = SongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = 180_000L,
        contentUri = "content://media/external/audio/media/$id",
        dataPath = "/storage/emulated/0/Music/$title.mp3",
        trackNumber = 1,
        year = 2024,
        dateAdded = System.currentTimeMillis()
    )

    // ===== Songs Tests =====

    @Test
    fun `getSongs should return songs from database`() = runTest {
        // Given
        songDao.insertAll(listOf(
            createTestSongEntity(1, "Song A"),
            createTestSongEntity(2, "Song B"),
            createTestSongEntity(3, "Song C")
        ))

        // When & Then
        repository.getSongs().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("Song A", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSong should return specific song`() = runTest {
        // Given
        songDao.insertAll(listOf(createTestSongEntity(42, "特定歌曲")))

        // When & Then
        repository.getSong(42).test {
            val result = awaitItem()
            assertEquals(42L, result?.id)
            assertEquals("特定歌曲", result?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchSongs should find songs by title or artist`() = runTest {
        // Given
        songDao.insertAll(listOf(
            createTestSongEntity(1, "Love Song", artist = "周杰倫"),
            createTestSongEntity(2, "Hate Song", artist = "林俊傑"),
            createTestSongEntity(3, "Dance", artist = "周杰倫")
        ))

        // When & Then - 搜尋藝人
        repository.searchSongs("周杰倫").test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== Albums Tests =====

    @Test
    fun `getAlbums should group songs by albumId`() = runTest {
        // Given
        songDao.insertAll(listOf(
            createTestSongEntity(1, "Song 1", album = "Album A", albumId = 100),
            createTestSongEntity(2, "Song 2", album = "Album A", albumId = 100),
            createTestSongEntity(3, "Song 3", album = "Album B", albumId = 200)
        ))

        // When & Then
        repository.getAlbums().test {
            val result = awaitItem()
            assertEquals(2, result.size)

            val albumA = result.find { it.title == "Album A" }
            assertEquals(2, albumA?.songCount)

            val albumB = result.find { it.title == "Album B" }
            assertEquals(1, albumB?.songCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== Artists Tests =====

    @Test
    fun `getArtists should group songs by artist`() = runTest {
        // Given
        songDao.insertAll(listOf(
            createTestSongEntity(1, "Song 1", artist = "Artist A"),
            createTestSongEntity(2, "Song 2", artist = "Artist A"),
            createTestSongEntity(3, "Song 3", artist = "Artist A"),
            createTestSongEntity(4, "Song 4", artist = "Artist B")
        ))

        // When & Then
        repository.getArtists().test {
            val result = awaitItem()
            assertEquals(2, result.size)

            val artistA = result.find { it.name == "Artist A" }
            assertEquals(3, artistA?.songCount)

            val artistB = result.find { it.name == "Artist B" }
            assertEquals(1, artistB?.songCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== Scan Music Tests =====

    @Test
    fun `scanLocalMusic should update database with songs from LocalAudioSource`() = runTest {
        // Given
        val songsFromSystem = listOf(
            createTestSong(1, "System Song 1"),
            createTestSong(2, "System Song 2")
        )
        coEvery { localAudioSource.loadMusic(any(), any()) } returns songsFromSystem

        // When
        repository.scanLocalMusic().test {
            // 初始化狀態
            val scanning1 = awaitItem()
            assertTrue(scanning1 is ScanStatus.Scanning)

            // 處理中狀態
            val scanning2 = awaitItem()
            assertTrue(scanning2 is ScanStatus.Scanning)

            // 完成狀態
            val completed = awaitItem()
            assertTrue(completed is ScanStatus.Completed)
            assertEquals(2, (completed as ScanStatus.Completed).totalAdded)

            awaitComplete()
        }

        // Then - 驗證資料庫已更新
        repository.getSongs().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `scanLocalMusic should emit Failed status on error`() = runTest {
        // Given
        coEvery { localAudioSource.loadMusic(any(), any()) } throws RuntimeException("Permission denied")

        // When & Then
        repository.scanLocalMusic().test {
            awaitItem() // Initial scanning status

            val failed = awaitItem()
            assertTrue(failed is ScanStatus.Failed)
            assertEquals("Permission denied", (failed as ScanStatus.Failed).error)

            awaitComplete()
        }
    }

    // ===== Favorites Tests =====

    @Test
    fun `toggleFavorite should add song to favorites when not favorited`() = runTest {
        // Given
        songDao.insertAll(listOf(createTestSongEntity(1, "Test Song")))

        // Initially not favorited
        assertFalse(repository.isSongFavorite(1).first())

        // When
        repository.toggleFavorite(1)

        // Then
        assertTrue(repository.isSongFavorite(1).first())
    }

    @Test
    fun `toggleFavorite should remove song from favorites when already favorited`() = runTest {
        // Given
        songDao.insertAll(listOf(createTestSongEntity(1, "Test Song")))
        favoriteDao.addFavorite(FavoriteEntity(1))

        // Initially favorited
        assertTrue(repository.isSongFavorite(1).first())

        // When
        repository.toggleFavorite(1)

        // Then
        assertFalse(repository.isSongFavorite(1).first())
    }

    @Test
    fun `getFavoriteSongs should return only favorited songs`() = runTest {
        // Given
        songDao.insertAll(listOf(
            createTestSongEntity(1, "Song 1"),
            createTestSongEntity(2, "Song 2"),
            createTestSongEntity(3, "Song 3")
        ))
        favoriteDao.addFavorite(FavoriteEntity(1))
        favoriteDao.addFavorite(FavoriteEntity(3))

        // When & Then
        repository.getFavoriteSongs().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.any { it.id == 1L })
            assertTrue(result.any { it.id == 3L })
            assertFalse(result.any { it.id == 2L })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===== Playlist Tests =====

    @Test
    fun `createPlaylist should insert new playlist and return id`() = runTest {
        // When
        val playlistId = repository.createPlaylist("My Playlist")

        // Then
        assertTrue(playlistId > 0)

        repository.getPlaylist(playlistId).test {
            val result = awaitItem()
            assertEquals("My Playlist", result?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addSongToPlaylist should associate song with playlist`() = runTest {
        // Given
        songDao.insertAll(listOf(createTestSongEntity(1, "Test Song")))
        val playlistId = repository.createPlaylist("Test Playlist")

        // When
        repository.addSongToPlaylist(playlistId, 1)

        // Then
        repository.getSongsForPlaylist(playlistId).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Test Song", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeSongFromPlaylist should remove association`() = runTest {
        // Given
        songDao.insertAll(listOf(createTestSongEntity(1, "Test Song")))
        val playlistId = repository.createPlaylist("Test Playlist")
        repository.addSongToPlaylist(playlistId, 1)

        // When
        repository.removeSongFromPlaylist(playlistId, 1)

        // Then
        repository.getSongsForPlaylist(playlistId).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deletePlaylist should remove playlist`() = runTest {
        // Given
        val playlistId = repository.createPlaylist("To Delete")

        // When
        repository.deletePlaylist(playlistId)

        // Then
        repository.getPlaylist(playlistId).test {
            val result = awaitItem()
            assertEquals(null, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `renamePlaylist should update playlist name`() = runTest {
        // Given
        val playlistId = repository.createPlaylist("Old Name")

        // When
        repository.renamePlaylist(playlistId, "New Name")

        // Then
        repository.getPlaylist(playlistId).test {
            val result = awaitItem()
            assertEquals("New Name", result?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
