package com.gemini.music.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 自定義歌詞資料存取物件。
 */
@Dao
interface CustomLyricsDao {
    
    @Query("SELECT * FROM custom_lyrics WHERE songId = :songId")
    fun getLyricsBySongId(songId: Long): Flow<CustomLyricsEntity?>
    
    @Query("SELECT * FROM custom_lyrics WHERE songId = :songId")
    suspend fun getLyricsBySongIdSync(songId: Long): CustomLyricsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lyrics: CustomLyricsEntity)
    
    @Query("UPDATE custom_lyrics SET globalOffset = :offset WHERE songId = :songId")
    suspend fun updateOffset(songId: Long, offset: Long)
    
    @Query("DELETE FROM custom_lyrics WHERE songId = :songId")
    suspend fun delete(songId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM custom_lyrics WHERE songId = :songId)")
    fun hasCustomLyrics(songId: Long): Flow<Boolean>
    
    @Query("SELECT COUNT(*) FROM custom_lyrics")
    suspend fun getCount(): Int
}
