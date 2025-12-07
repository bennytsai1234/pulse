package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.Album
import com.gemini.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAlbumByIdUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    // Note: This is inefficient. The repository should have a dedicated getAlbumById method.
    // For now, we encapsulate the existing logic.
    operator fun invoke(albumId: Long): Flow<Album?> {
        return musicRepository.getAlbums().map { albums ->
            albums.find { it.id == albumId }
        }
    }
}
