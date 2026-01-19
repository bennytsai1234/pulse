package com.pulse.music.data.repository

import com.pulse.music.data.database.PlaybackHistoryDao
import com.pulse.music.data.database.asDomainModel
import com.pulse.music.data.database.asEntity
import com.pulse.music.domain.model.ArtistPlayStats
import com.pulse.music.domain.model.PlaybackRecord
import com.pulse.music.domain.model.SongPlayStats
import com.pulse.music.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val playbackHistoryDao: PlaybackHistoryDao
) : StatsRepository {

    override suspend fun recordPlay(record: PlaybackRecord) {
        playbackHistoryDao.insertRecord(record.asEntity())
    }

    override fun getRecentlyPlayed(limit: Int): Flow<List<PlaybackRecord>> {
        return playbackHistoryDao.getRecentlyPlayed(limit).map { entities ->
            entities.map { it.asDomainModel() }
        }
    }

    override fun getMostPlayedSongs(limit: Int): Flow<List<SongPlayStats>> {
        return playbackHistoryDao.getMostPlayedSongs(limit).map { entities ->
            entities.map { 
                SongPlayStats(
                    songId = it.songId,
                    songTitle = it.songTitle,
                    artistName = it.artistName,
                    albumName = it.albumName,
                    albumArtUri = it.albumArtUri,
                    totalPlayCount = it.playCount,
                    totalPlayTime = it.totalTime,
                    lastPlayedAt = it.lastPlayed
                )
            }
        }
    }

    override fun getMostPlayedArtists(limit: Int): Flow<List<ArtistPlayStats>> {
        return playbackHistoryDao.getMostPlayedArtists(limit).map { entities ->
            entities.map { 
                ArtistPlayStats(
                    artistName = it.artistName,
                    totalPlayCount = it.playCount,
                    totalPlayTime = it.totalTime,
                    songCount = it.songCount
                )
            }
        }
    }

    override fun getHistory(): Flow<List<PlaybackRecord>> {
        return getRecentlyPlayed(1000) // Default limit
    }
}
