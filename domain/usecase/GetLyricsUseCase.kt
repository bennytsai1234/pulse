package com.sigma.music.domain.usecase

import com.sigma.music.domain.model.LyricLine
import com.sigma.music.domain.repository.LyricsRepository
import javax.inject.Inject

class GetLyricsUseCase @Inject constructor(
    private val lyricsRepository: LyricsRepository
) {
    suspend operator fun invoke(path: String): List<LyricLine> {
        return lyricsRepository.getLyrics(path)
    }
}
