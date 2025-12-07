package com.gemini.music.domain.repository

interface WaveformRepository {
    /**
     * Extracts waveform amplitudes from the given audio file path.
     * Returns a list of normalized amplitudes (0..1) or null if extraction fails.
     */
    suspend fun extractWaveform(filePath: String): List<Int>
}
