package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.Album
import com.gemini.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Album>> {
        return musicRepository.getAlbums()
    }
}
