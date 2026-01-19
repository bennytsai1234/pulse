package com.pulse.music.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSong(id: Long): Flow<SongEntity?>
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongSync(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 20")
    fun getRecentlyAdded(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber ASC")
    fun getSongsByAlbumId(albumId: Long): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Query("DELETE FROM songs")
    suspend fun clearAll()

    // 交易操作：清空舊資料並寫入新資料 (全量更新策略)
    @Transaction
    suspend fun updateMusicLibrary(songs: List<SongEntity>) {
        clearAll()
        insertAll(songs)
    }

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: Long)

    @Query("SELECT * FROM songs ORDER BY title ASC LIMIT :limit OFFSET :offset")
    suspend fun getSongsPaged(limit: Int, offset: Int): List<SongEntity>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit OFFSET :offset")
    suspend fun getSongsPagedByDateAdded(limit: Int, offset: Int): List<SongEntity>

    @Query("SELECT * FROM songs ORDER BY artist ASC, title ASC LIMIT :limit OFFSET :offset")
    suspend fun getSongsPagedByArtist(limit: Int, offset: Int): List<SongEntity>

    @Query("SELECT * FROM songs ORDER BY album ASC, trackNumber ASC LIMIT :limit OFFSET :offset")
    suspend fun getSongsPagedByAlbum(limit: Int, offset: Int): List<SongEntity>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY title ASC LIMIT :limit OFFSET :offset")
    suspend fun searchSongsPaged(query: String, limit: Int, offset: Int): List<SongEntity>
}

