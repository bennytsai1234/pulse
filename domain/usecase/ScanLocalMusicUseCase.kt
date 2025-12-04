package com.sigma.music.domain.usecase

import com.sigma.music.domain.repository.MusicRepository
import javax.inject.Inject

class ScanLocalMusicUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke() {
        musicRepository.scanLocalMusic()
    }
}
