package com.pulse.music.domain.usecase.stats

import com.pulse.music.domain.model.PlaybackRecord
import com.pulse.music.domain.model.Song
import com.pulse.music.domain.repository.StatsRepository
import javax.inject.Inject

class RecordPlayUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(song: Song, durationPlayed: Long, completed: Boolean = false) {
        val record = PlaybackRecord(
            songId = song.id,
            songTitle = song.title,
            artistName = song.artist,
            albumName = song.album,
            playedAt = System.currentTimeMillis(),
            durationPlayed = durationPlayed,
            completed = completed
        )
        statsRepository.recordPlay(record)
    }
}
