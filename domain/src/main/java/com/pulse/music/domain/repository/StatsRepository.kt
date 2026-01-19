package com.pulse.music.domain.repository

import com.pulse.music.domain.model.ArtistPlayStats
import com.pulse.music.domain.model.PlaybackRecord
import com.pulse.music.domain.model.SongPlayStats
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    suspend fun recordPlay(record: PlaybackRecord)
    
    fun getRecentlyPlayed(limit: Int): Flow<List<PlaybackRecord>>
    
    fun getMostPlayedSongs(limit: Int): Flow<List<SongPlayStats>>
    
    fun getMostPlayedArtists(limit: Int): Flow<List<ArtistPlayStats>>
    
    fun getHistory(): Flow<List<PlaybackRecord>>
}
