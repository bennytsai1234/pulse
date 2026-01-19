package com.pulse.music.domain.usecase

import com.pulse.music.domain.model.Genre
import com.pulse.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGenresUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Genre>> {
        // Implementation pending: We need to aggregate genres from songs or use a GenreDao
        // Since we added 'genre' to Song, we can group by that.
        // Assuming MusicRepository has a method to get all songs or specific query.
        // For efficiency, we should add getGenres() to MusicRepository/SongDao.
        return musicRepository.getGenres()
    }
}
