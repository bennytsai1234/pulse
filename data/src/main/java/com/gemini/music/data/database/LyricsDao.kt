package com.gemini.music.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LyricsDao {
    
    @Query("SELECT * FROM lyrics_cache WHERE songId = :songId LIMIT 1")
    suspend fun getLyrics(songId: Long): LyricsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)
    
    @Query("DELETE FROM lyrics_cache WHERE songId = :songId")
    suspend fun deleteLyrics(songId: Long)
    
    @Query("DELETE FROM lyrics_cache")
    suspend fun clearAll()
}
