package com.sigma.music.domain.usecase

import com.sigma.music.domain.model.Album
import com.sigma.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Album>> {
        return musicRepository.getAlbums()
    }
}
