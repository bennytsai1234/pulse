package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.LyricLine
import com.gemini.music.domain.repository.LyricsRepository
import javax.inject.Inject

class GetLyricsUseCase @Inject constructor(
    private val lyricsRepository: LyricsRepository
) {
    suspend operator fun invoke(path: String): List<LyricLine> {
        return lyricsRepository.getLyrics(path)
    }
}
