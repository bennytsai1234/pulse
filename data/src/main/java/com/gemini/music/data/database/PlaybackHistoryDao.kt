package com.gemini.music.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {
    
    // ==================== Insert ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PlaybackHistoryEntity): Long
    
    // ==================== Query - Recent ====================
    
    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<PlaybackHistoryEntity>>
    
    @Query("SELECT DISTINCT songId, songTitle, artistName, albumName, albumArtUri, MAX(playedAt) as playedAt, SUM(durationPlayed) as durationPlayed, MAX(completed) as completed, id FROM playback_history GROUP BY songId ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayedDistinct(limit: Int = 50): Flow<List<PlaybackHistoryEntity>>
    
    // ==================== Query - Most Played ====================
    
    @Query("""
        SELECT songId, songTitle, artistName, albumName, albumArtUri, 
               COUNT(*) as playCount, SUM(durationPlayed) as totalTime, MAX(playedAt) as lastPlayed
        FROM playback_history 
        GROUP BY songId 
        ORDER BY playCount DESC 
        LIMIT :limit
    """)
    fun getMostPlayedSongs(limit: Int = 50): Flow<List<SongPlayStatsProjection>>
    
    @Query("""
        SELECT artistName, COUNT(*) as playCount, SUM(durationPlayed) as totalTime, COUNT(DISTINCT songId) as songCount
        FROM playback_history 
        GROUP BY artistName 
        ORDER BY playCount DESC 
        LIMIT :limit
    """)
    fun getMostPlayedArtists(limit: Int = 20): Flow<List<ArtistPlayStatsProjection>>
    
    // ==================== Query - Time Range ====================
    
    @Query("SELECT * FROM playback_history WHERE playedAt >= :startTime ORDER BY playedAt DESC")
    fun getPlayedSince(startTime: Long): Flow<List<PlaybackHistoryEntity>>
    
    @Query("SELECT DISTINCT songId FROM playback_history WHERE playedAt >= :startTime")
    fun getSongIdsPlayedSince(startTime: Long): Flow<List<Long>>
    
    // ==================== Query - Statistics ====================
    
    @Query("SELECT COUNT(*) FROM playback_history")
    suspend fun getTotalPlayCount(): Int
    
    @Query("SELECT SUM(durationPlayed) FROM playback_history")
    suspend fun getTotalPlayTime(): Long?
    
    @Query("SELECT COUNT(DISTINCT songId) FROM playback_history")
    suspend fun getUniqueSongsCount(): Int
    
    @Query("SELECT COUNT(DISTINCT artistName) FROM playback_history")
    suspend fun getUniqueArtistsCount(): Int
    
    @Query("SELECT COUNT(*) FROM playback_history WHERE playedAt >= :startTime")
    suspend fun getPlayCountSince(startTime: Long): Int
    
    @Query("SELECT SUM(durationPlayed) FROM playback_history WHERE playedAt >= :startTime")
    suspend fun getPlayTimeSince(startTime: Long): Long?
    
    // ==================== Query - Never Played ====================
    
    @Query("SELECT DISTINCT songId FROM playback_history")
    suspend fun getAllPlayedSongIds(): List<Long>
    
    // ==================== Delete ====================
    
    @Query("DELETE FROM playback_history WHERE playedAt < :beforeTime")
    suspend fun deleteOldRecords(beforeTime: Long)
    
    @Query("DELETE FROM playback_history")
    suspend fun clearAll()
}

/**
 * Projection for song play statistics query
 */
data class SongPlayStatsProjection(
    val songId: Long,
    val songTitle: String,
    val artistName: String,
    val albumName: String,
    val albumArtUri: String?,
    val playCount: Int,
    val totalTime: Long,
    val lastPlayed: Long
)

/**
 * Projection for artist play statistics query
 */
data class ArtistPlayStatsProjection(
    val artistName: String,
    val playCount: Int,
    val totalTime: Long,
    val songCount: Int
)
