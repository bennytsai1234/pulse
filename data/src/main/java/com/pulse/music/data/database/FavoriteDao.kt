package com.pulse.music.data.database

import androidx.room.Dao
import androidx.room.RoomWarnings
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Query("SELECT * FROM songs INNER JOIN favorites ON songs.id = favorites.songId ORDER BY favorites.dateAdded DESC")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    fun getFavoriteSongs(): Flow<List<SongEntity>>
}
