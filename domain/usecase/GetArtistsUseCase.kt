package com.sigma.music.domain.usecase

import com.sigma.music.domain.model.Artist
import com.sigma.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArtistsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Artist>> {
        return musicRepository.getArtists()
    }
}
