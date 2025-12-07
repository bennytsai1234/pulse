package com.gemini.music.domain.repository

import com.gemini.music.domain.model.LyricLine

interface LyricsRepository {
    suspend fun getLyrics(audioPath: String): List<LyricLine>
}
