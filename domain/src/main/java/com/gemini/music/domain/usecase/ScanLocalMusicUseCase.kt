package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicRepository
import javax.inject.Inject

class ScanLocalMusicUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke() {
        musicRepository.scanLocalMusic()
    }
}
