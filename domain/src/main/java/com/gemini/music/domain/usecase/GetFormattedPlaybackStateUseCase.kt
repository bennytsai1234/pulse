package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.LyricLine
import com.gemini.music.domain.repository.MusicController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class FormattedPlaybackState(
    val progress: Float = 0f,
    val currentTime: String = "0:00",
    val totalTime: String = "0:00",
    val currentLyricIndex: Int = -1
)

class GetFormattedPlaybackStateUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(lyrics: List<LyricLine>): Flow<FormattedPlaybackState> = flow {
        while (true) {
            val isPlaying = musicController.musicState.value.isPlaying
            val currentPos = musicController.getCurrentPosition()
            val duration = musicController.getDuration()
            
            if (duration > 0) {
                val progress = currentPos.toFloat() / duration.toFloat()
                val lyricIndex = findLyricIndex(lyrics, currentPos)
                
                emit(
                    FormattedPlaybackState(
                        progress = progress,
                        currentTime = formatTime(currentPos),
                        totalTime = formatTime(duration),
                        currentLyricIndex = lyricIndex
                    )
                )
            } else {
                 emit(FormattedPlaybackState())
            }

            delay(200)
        }
    }

    private fun findLyricIndex(lyrics: List<LyricLine>, currentPos: Long): Int {
        if (lyrics.isEmpty()) return -1
        return lyrics.indexOfLast { it.startTime <= currentPos }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
