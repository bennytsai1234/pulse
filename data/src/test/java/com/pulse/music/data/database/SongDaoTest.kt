package com.pulse.music.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

/**
 * SongDao 整合測試
 * 
 * 使用 Robolectric 模擬 Android 環境測試 Room Database 操作
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class SongDaoTest {

    private lateinit var database: PulseDatabase
    private lateinit var songDao: SongDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        songDao = database.songDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ===== 測試用假資料 =====
    private fun createTestSongEntity(
        id: Long = 1L,
        title: String = "Test Song",
        artist: String = "Test Artist",
        album: String = "Test Album",
        albumId: Long = 100L,
        duration: Long = 180_000L,
        dateAdded: Long = System.currentTimeMillis()
    ) = SongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        contentUri = "content://media/external/audio/media/$id",
        dataPath = "/storage/emulated/0/Music/$title.mp3",
        trackNumber = 1,
        year = 2024,
        dateAdded = dateAdded
    )

    @Test
    fun `insertAll should add songs to database`() = runTest {
        // Given
        val songs = listOf(
            createTestSongEntity(1, "Song A"),
            createTestSongEntity(2, "Song B"),
            createTestSongEntity(3, "Song C")
        )

        // When
        songDao.insertAll(songs)

        // Then
        songDao.getAllSongs().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllSongs should return songs sorted by title`() = runTest {
        // Given
        val songs = listOf(
            createTestSongEntity(1, "Zebra"),
            createTestSongEntity(2, "Apple"),
            createTestSongEntity(3, "Mango")
        )
        songDao.insertAll(songs)

        // When & Then
        songDao.getAllSongs().test {
            val result = awaitItem()
            assertEquals("Apple", result[0].title)
            assertEquals("Mango", result[1].title)
            assertEquals("Zebra", result[2].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSong should return specific song by id`() = runTest {
        // Given
        val song = createTestSongEntity(42, "特定歌曲")
        songDao.insertAll(listOf(song))

        // When & Then
        songDao.getSong(42).test {
            val result = awaitItem()
            assertEquals(42L, result?.id)
            assertEquals("特定歌曲", result?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSong should return null for non-existent id`() = runTest {
        // Given
        songDao.insertAll(listOf(createTestSongEntity(1, "Existing")))

        // When & Then
        songDao.getSong(999).test {
            val result = awaitItem()
            assertNull(result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchSongs should find songs by title`() = runTest {
        // Given
        val songs = listOf(
            createTestSongEntity(1, "Love Song"),
            createTestSongEntity(2, "Hate Song"),
            createTestSongEntity(3, "Dance Music")
        )
        songDao.insertAll(songs)

        // When & Then
        songDao.searchSongs("Love").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Love Song", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchSongs should find songs by artist`() = runTest {
        // Given
        val songs = listOf(
            createTestSongEntity(1, "Song 1", artist = "周杰倫"),
            createTestSongEntity(2, "Song 2", artist = "林俊傑"),
            createTestSongEntity(3, "Song 3", artist = "周杰倫")
        )
        songDao.insertAll(songs)

        // When & Then
        songDao.searchSongs("周杰倫").test {
            val result = awaitItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecentlyAdded should return max 20 songs sorted by dateAdded desc`() = runTest {
        // Given - 新增 25 首歌，確保只回傳最新 20 首
        val songs = (1..25).map { i ->
            createTestSongEntity(
                id = i.toLong(),
                title = "Song $i",
                dateAdded = i * 1000L // 越大越新
            )
        }
        songDao.insertAll(songs)

        // When & Then
        songDao.getRecentlyAdded().test {
            val result = awaitItem()
            assertEquals(20, result.size)
            // 最新的應該排在最前面
            assertEquals("Song 25", result[0].title)
            assertEquals("Song 6", result[19].title) // 第 20 首 = Song 6
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSongsByAlbumId should return songs from specific album sorted by track number`() = runTest {
        // Given
        val songs = listOf(
            createTestSongEntity(1, "Track 3", albumId = 100).copy(trackNumber = 3),
            createTestSongEntity(2, "Track 1", albumId = 100).copy(trackNumber = 1),
            createTestSongEntity(3, "Track 2", albumId = 100).copy(trackNumber = 2),
            createTestSongEntity(4, "Other Album", albumId = 200).copy(trackNumber = 1)
        )
        songDao.insertAll(songs)

        // When & Then
        songDao.getSongsByAlbumId(100).test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("Track 1", result[0].title)
            assertEquals("Track 2", result[1].title)
            assertEquals("Track 3", result[2].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearAll should remove all songs`() = runTest {
        // Given
        songDao.insertAll(listOf(
            createTestSongEntity(1, "Song 1"),
            createTestSongEntity(2, "Song 2")
        ))

        // When
        songDao.clearAll()

        // Then
        songDao.getAllSongs().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateMusicLibrary should replace all songs atomically`() = runTest {
        // Given - 先插入舊資料
        songDao.insertAll(listOf(
            createTestSongEntity(1, "Old Song 1"),
            createTestSongEntity(2, "Old Song 2")
        ))

        // When - 用新資料完全取代
        val newSongs = listOf(
            createTestSongEntity(3, "New Song A"),
            createTestSongEntity(4, "New Song B"),
            createTestSongEntity(5, "New Song C")
        )
        songDao.updateMusicLibrary(newSongs)

        // Then
        songDao.getAllSongs().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            // 確認舊資料已被刪除
            assertTrue(result.none { it.title.startsWith("Old") })
            // 確認新資料已寫入
            assertEquals("New Song A", result[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert with REPLACE strategy should update existing song`() = runTest {
        // Given
        val original = createTestSongEntity(1, "Original Title")
        songDao.insertAll(listOf(original))

        // When - 使用相同 ID 插入不同資料
        val updated = original.copy(title = "Updated Title")
        songDao.insertAll(listOf(updated))

        // Then
        songDao.getSong(1).test {
            val result = awaitItem()
            assertEquals("Updated Title", result?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
