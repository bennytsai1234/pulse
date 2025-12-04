package com.sigma.music.domain.repository

import com.sigma.music.domain.model.LyricLine

interface LyricsRepository {
    suspend fun getLyrics(audioPath: String): List<LyricLine>
}
