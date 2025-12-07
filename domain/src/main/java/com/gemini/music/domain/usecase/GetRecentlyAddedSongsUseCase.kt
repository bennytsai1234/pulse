package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentlyAddedSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return musicRepository.getRecentlyAdded()
    }
}
