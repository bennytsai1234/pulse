package com.pulse.music.domain.usecase.playlist

import com.pulse.music.domain.model.Playlist
import com.pulse.music.domain.model.Song
import com.pulse.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

/**
 * Generates a "Mood" playlist based on simple heuristics (e.g. Random for now, BPM later).
 */
class GetMoodPlaylistUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    // Moods: Energetic, Calm, Focus, Melancholy
    enum class Mood { ENERGETIC, CALM, FOCUS, MELANCHOLY }

    operator fun invoke(mood: Mood): Flow<List<Song>> = flow {
        val allSongs = musicRepository.getSongs().first()
        if (allSongs.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        // Mock Implementation: Random shuffle for now until we have BPM/Features
        // In future: 
        // Energetic -> High BPM
        // Calm -> Low BPM, Acoustic
        val shuffled = allSongs.shuffled(Random(System.currentTimeMillis()))
        emit(shuffled.take(20))
    }
}
