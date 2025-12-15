package com.gemini.music.data.repository

import com.gemini.music.data.database.PlaybackHistoryDao
import com.gemini.music.data.database.PlaybackHistoryEntity
import com.gemini.music.data.database.SongDao
import com.gemini.music.data.database.FavoriteDao
import com.gemini.music.data.database.asDomainModel
import com.gemini.music.domain.model.ArtistPlayStats
import com.gemini.music.domain.model.ListeningStatsOverview
import com.gemini.music.domain.model.PlaybackRecord
import com.gemini.music.domain.model.SmartPlaylist
import com.gemini.music.domain.model.SmartPlaylistType
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.model.SongPlayStats
import com.gemini.music.domain.repository.ListeningStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListeningStatsRepositoryImpl @Inject constructor(
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val songDao: SongDao,
    private val favoriteDao: FavoriteDao
) : ListeningStatsRepository {
    
    override suspend fun recordPlayback(song: Song, durationPlayed: Long, completed: Boolean) {
        val entity = PlaybackHistoryEntity(
            songId = song.id,
            songTitle = song.title,
            artistName = song.artist,
            albumName = song.album,
            albumArtUri = song.albumArtUri,
            playedAt = System.currentTimeMillis(),
            durationPlayed = durationPlayed,
            completed = completed
        )
        playbackHistoryDao.insertRecord(entity)
    }
    
    override fun getRecentlyPlayed(limit: Int): Flow<List<PlaybackRecord>> {
        return playbackHistoryDao.getRecentlyPlayedDistinct(limit).map { entities ->
            entities.map { it.asDomainModel() }
        }.flowOn(Dispatchers.IO)
    }
    
    override fun getMostPlayedSongs(limit: Int): Flow<List<SongPlayStats>> {
        return playbackHistoryDao.getMostPlayedSongs(limit).map { projections ->
            projections.map { proj ->
                SongPlayStats(
                    songId = proj.songId,
                    songTitle = proj.songTitle,
                    artistName = proj.artistName,
                    albumName = proj.albumName,
                    albumArtUri = proj.albumArtUri,
                    totalPlayCount = proj.playCount,
                    totalPlayTime = proj.totalTime,
                    lastPlayedAt = proj.lastPlayed
                )
            }
        }.flowOn(Dispatchers.IO)
    }
    
    override fun getMostPlayedArtists(limit: Int): Flow<List<ArtistPlayStats>> {
        return playbackHistoryDao.getMostPlayedArtists(limit).map { projections ->
            projections.map { proj ->
                ArtistPlayStats(
                    artistName = proj.artistName,
                    totalPlayCount = proj.playCount,
                    totalPlayTime = proj.totalTime,
                    songCount = proj.songCount
                )
            }
        }.flowOn(Dispatchers.IO)
    }
    
    override suspend fun getStatsOverview(): ListeningStatsOverview {
        val totalPlayCount = playbackHistoryDao.getTotalPlayCount()
        val totalPlayTime = playbackHistoryDao.getTotalPlayTime() ?: 0L
        val uniqueSongs = playbackHistoryDao.getUniqueSongsCount()
        val uniqueArtists = playbackHistoryDao.getUniqueArtistsCount()
        
        val topSongs = getMostPlayedSongs(10).first()
        val topArtists = getMostPlayedArtists(10).first()
        val recentlyPlayed = getRecentlyPlayed(10).first()
        
        // Calculate average daily play time (last 30 days)
        val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        val recentPlayTime = playbackHistoryDao.getPlayTimeSince(thirtyDaysAgo) ?: 0L
        val avgDailyPlayTime = recentPlayTime / 30
        
        return ListeningStatsOverview(
            totalPlayCount = totalPlayCount,
            totalPlayTime = totalPlayTime,
            uniqueSongsPlayed = uniqueSongs,
            uniqueArtistsPlayed = uniqueArtists,
            averageDailyPlayTime = avgDailyPlayTime,
            mostPlayedSong = topSongs.firstOrNull(),
            mostPlayedArtist = topArtists.firstOrNull(),
            recentlyPlayed = recentlyPlayed,
            topSongs = topSongs,
            topArtists = topArtists
        )
    }
    
    override suspend fun getTotalPlayCount(): Int {
        return playbackHistoryDao.getTotalPlayCount()
    }
    
    override suspend fun getTotalPlayTime(): Long {
        return playbackHistoryDao.getTotalPlayTime() ?: 0L
    }
    
    override fun getSongsForSmartPlaylist(type: SmartPlaylistType, limit: Int): Flow<List<Song>> {
        return when (type) {
            SmartPlaylistType.MOST_PLAYED -> {
                getMostPlayedSongs(limit).map { stats ->
                    val songIds = stats.map { it.songId }
                    songDao.getAllSongs().first()
                        .filter { it.id in songIds }
                        .sortedBy { song -> songIds.indexOf(song.id) }
                        .map { it.asDomainModel() }
                }
            }
            SmartPlaylistType.RECENTLY_PLAYED -> {
                getRecentlyPlayed(limit).map { records ->
                    val songIds = records.map { it.songId }
                    songDao.getAllSongs().first()
                        .filter { it.id in songIds }
                        .sortedBy { song -> songIds.indexOf(song.id) }
                        .map { it.asDomainModel() }
                }
            }
            SmartPlaylistType.RECENTLY_ADDED -> {
                songDao.getRecentlyAdded().map { entities ->
                    entities.take(limit).map { it.asDomainModel() }
                }
            }
            SmartPlaylistType.NEVER_PLAYED -> {
                songDao.getAllSongs().map { allSongs ->
                    val playedIds = playbackHistoryDao.getAllPlayedSongIds().toSet()
                    allSongs.filter { it.id !in playedIds }
                        .take(limit)
                        .map { it.asDomainModel() }
                }
            }
            SmartPlaylistType.FAVORITES -> {
                favoriteDao.getFavoriteSongs().map { songs ->
                    songs.take(limit).map { it.asDomainModel() }
                }
            }
            SmartPlaylistType.LONG_SONGS -> {
                songDao.getAllSongs().map { songs ->
                    songs.filter { it.duration > 5 * 60 * 1000 } // > 5 minutes
                        .sortedByDescending { it.duration }
                        .take(limit)
                        .map { it.asDomainModel() }
                }
            }
            SmartPlaylistType.SHORT_SONGS -> {
                songDao.getAllSongs().map { songs ->
                    songs.filter { it.duration < 3 * 60 * 1000 } // < 3 minutes
                        .sortedBy { it.duration }
                        .take(limit)
                        .map { it.asDomainModel() }
                }
            }
            SmartPlaylistType.THIS_WEEK -> {
                val weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
                playbackHistoryDao.getSongIdsPlayedSince(weekAgo).map { ids ->
                    songDao.getAllSongs().first()
                        .filter { it.id in ids }
                        .take(limit)
                        .map { it.asDomainModel() }
                }
            }
            SmartPlaylistType.THIS_MONTH -> {
                val monthAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
                playbackHistoryDao.getSongIdsPlayedSince(monthAgo).map { ids ->
                    songDao.getAllSongs().first()
                        .filter { it.id in ids }
                        .take(limit)
                        .map { it.asDomainModel() }
                }
            }
        }.flowOn(Dispatchers.IO)
    }
    
    override fun getSmartPlaylists(): Flow<List<SmartPlaylist>> {
        return combine(
            songDao.getAllSongs(),
            playbackHistoryDao.getMostPlayedSongs(100),
            favoriteDao.getFavoriteSongs()
        ) { allSongs, mostPlayed, favorites ->
            val playedIds = mostPlayed.map { it.songId }.toSet()
            val neverPlayedCount = allSongs.count { it.id !in playedIds }
            val longSongsCount = allSongs.count { it.duration > 5 * 60 * 1000 }
            val shortSongsCount = allSongs.count { it.duration < 3 * 60 * 1000 }
            
            SmartPlaylist.createAll().map { playlist ->
                when (playlist.type) {
                    SmartPlaylistType.MOST_PLAYED -> playlist.copy(songCount = mostPlayed.size.coerceAtMost(50))
                    SmartPlaylistType.RECENTLY_PLAYED -> playlist.copy(songCount = mostPlayed.size.coerceAtMost(50))
                    SmartPlaylistType.RECENTLY_ADDED -> playlist.copy(songCount = allSongs.size.coerceAtMost(20))
                    SmartPlaylistType.NEVER_PLAYED -> playlist.copy(songCount = neverPlayedCount)
                    SmartPlaylistType.FAVORITES -> playlist.copy(songCount = favorites.size)
                    SmartPlaylistType.LONG_SONGS -> playlist.copy(songCount = longSongsCount)
                    SmartPlaylistType.SHORT_SONGS -> playlist.copy(songCount = shortSongsCount)
                    SmartPlaylistType.THIS_WEEK -> playlist.copy(songCount = 0) // Dynamic
                    SmartPlaylistType.THIS_MONTH -> playlist.copy(songCount = 0) // Dynamic
                }
            }
        }.flowOn(Dispatchers.IO)
    }
    
    override fun getSongIdsPlayedInLastDays(days: Int): Flow<List<Long>> {
        val startTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return playbackHistoryDao.getSongIdsPlayedSince(startTime)
    }
    
    override suspend fun getNeverPlayedSongIds(allSongIds: List<Long>): List<Long> {
        val playedIds = playbackHistoryDao.getAllPlayedSongIds().toSet()
        return allSongIds.filter { it !in playedIds }
    }
    
    override suspend fun deleteOldRecords(daysToKeep: Int) {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysToKeep.toLong())
        playbackHistoryDao.deleteOldRecords(cutoffTime)
    }
    
    override suspend fun clearAllHistory() {
        playbackHistoryDao.clearAll()
    }
}
