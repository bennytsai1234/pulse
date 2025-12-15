package com.gemini.music.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Scrobble 資料存取物件。
 */
@Dao
interface ScrobbleDao {
    
    @Query("SELECT * FROM scrobbles ORDER BY timestamp DESC")
    fun getAllScrobbles(): Flow<List<ScrobbleEntity>>
    
    @Query("SELECT * FROM scrobbles WHERE status = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingScrobbles(): Flow<List<ScrobbleEntity>>
    
    @Query("SELECT * FROM scrobbles WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingScrobblesSync(): List<ScrobbleEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scrobble: ScrobbleEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scrobbles: List<ScrobbleEntity>)
    
    @Query("UPDATE scrobbles SET status = :status, scrobbledAt = :scrobbledAt WHERE id IN (:ids)")
    suspend fun updateStatus(ids: List<Long>, status: String, scrobbledAt: Long?)
    
    @Query("SELECT COUNT(*) FROM scrobbles WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getScrobbleCount(startTime: Long, endTime: Long): Int
    
    @Query("""
        SELECT artist, COUNT(*) as count 
        FROM scrobbles 
        GROUP BY artist 
        ORDER BY count DESC 
        LIMIT :limit
    """)
    fun getTopArtists(limit: Int): Flow<List<ArtistScrobbleCount>>
    
    @Query("""
        SELECT songId, COUNT(*) as count 
        FROM scrobbles 
        GROUP BY songId 
        ORDER BY count DESC 
        LIMIT :limit
    """)
    fun getTopSongs(limit: Int): Flow<List<SongScrobbleCount>>
    
    @Query("DELETE FROM scrobbles WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldScrobbles(beforeTimestamp: Long)
    
    @Query("DELETE FROM scrobbles WHERE id = :id")
    suspend fun delete(id: Long)
}

data class ArtistScrobbleCount(
    val artist: String,
    val count: Int
)

data class SongScrobbleCount(
    val songId: Long,
    val count: Int
)
