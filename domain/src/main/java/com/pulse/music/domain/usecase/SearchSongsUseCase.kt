package com.pulse.music.domain.usecase

import com.pulse.music.domain.model.Song
import com.pulse.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(query: String): Flow<List<Song>> {
        return repository.searchSongs(query)
    }
}
