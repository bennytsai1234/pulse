package com.pulse.music.domain.usecase

import androidx.paging.PagingData
import com.pulse.music.domain.model.Song
import com.pulse.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Retrieves a paged list of songs from the repository.
 *
 * This UseCase abstracts the data source and configuration for paging.
 */
class GetPagedSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    /**
     * @param sortOrder "TITLE", "DATE_ADDED", "ARTIST", "ALBUM"
     * @return Flow of [PagingData] containing [Song] objects.
     */
    operator fun invoke(sortOrder: String = "TITLE"): Flow<PagingData<Song>> {
        return repository.getPagedSongs(sortOrder)
    }
}
