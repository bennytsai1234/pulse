package com.pulse.music.domain.usecase.stats

import com.pulse.music.domain.model.Song
import com.pulse.music.domain.repository.MusicRepository
import com.pulse.music.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMostPlayedSongsUseCase @Inject constructor(
    private val statsRepository: StatsRepository,
    private val musicRepository: MusicRepository
) {
    operator fun invoke(limit: Int = 50): Flow<List<Song>> {
        return statsRepository.getMostPlayedSongs(limit).map { statsList ->
            // In a real app, we might want to map these stats back to full Song objects
            // or return a domain object that includes play count.
            // For now, let's assume StatsRepository returns a list of Songs or SongStats.
            // Based on DAO, it returns SongPlayStatsProjection.
            // We need to map projection to Song or a specific UI model.
            // Let's assume we map to Song for simplicity in "Most Played" playlist.
            statsList.map { stats ->
                Song(
                    id = stats.songId,
                    title = stats.songTitle,
                    artist = stats.artistName,
                    album = stats.albumName,
                    albumId = 0, // Missing in projection
                    duration = 0, // Missing
                    contentUri = "", // Missing
                    dataPath = "", // Missing
                    trackNumber = 0,
                    year = 0,
                    dateAdded = 0
                )
            }
        }
    }
}
