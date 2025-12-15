package com.gemini.music.domain.repository

import com.gemini.music.domain.model.ArtistPlayStats
import com.gemini.music.domain.model.ListeningStatsOverview
import com.gemini.music.domain.model.PlaybackRecord
import com.gemini.music.domain.model.SmartPlaylist
import com.gemini.music.domain.model.SmartPlaylistType
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.model.SongPlayStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository for listening history and statistics
 */
interface ListeningStatsRepository {
    
    // ==================== Record Playback ====================
    
    /**
     * Record a song play event
     */
    suspend fun recordPlayback(
        song: Song,
        durationPlayed: Long,
        completed: Boolean
    )
    
    // ==================== Recently Played ====================
    
    /**
     * Get recently played songs
     */
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<PlaybackRecord>>
    
    // ==================== Most Played ====================
    
    /**
     * Get most played songs
     */
    fun getMostPlayedSongs(limit: Int = 50): Flow<List<SongPlayStats>>
    
    /**
     * Get most played artists
     */
    fun getMostPlayedArtists(limit: Int = 20): Flow<List<ArtistPlayStats>>
    
    // ==================== Statistics ====================
    
    /**
     * Get overall listening statistics
     */
    suspend fun getStatsOverview(): ListeningStatsOverview
    
    /**
     * Get total play count
     */
    suspend fun getTotalPlayCount(): Int
    
    /**
     * Get total listening time in milliseconds
     */
    suspend fun getTotalPlayTime(): Long
    
    // ==================== Smart Playlists ====================
    
    /**
     * Get songs for a smart playlist
     */
    fun getSongsForSmartPlaylist(
        type: SmartPlaylistType,
        limit: Int = 100
    ): Flow<List<Song>>
    
    /**
     * Get all smart playlists with song counts
     */
    fun getSmartPlaylists(): Flow<List<SmartPlaylist>>
    
    // ==================== Time-based Queries ====================
    
    /**
     * Get song IDs played in the last N days
     */
    fun getSongIdsPlayedInLastDays(days: Int): Flow<List<Long>>
    
    /**
     * Get never played song IDs
     */
    suspend fun getNeverPlayedSongIds(allSongIds: List<Long>): List<Long>
    
    // ==================== Maintenance ====================
    
    /**
     * Delete old records (older than N days)
     */
    suspend fun deleteOldRecords(daysToKeep: Int = 365)
    
    /**
     * Clear all history
     */
    suspend fun clearAllHistory()
}
