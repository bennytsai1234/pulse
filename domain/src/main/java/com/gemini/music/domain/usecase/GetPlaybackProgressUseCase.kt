package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPlaybackProgressUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(): Flow<Float> = flow {
        while (true) {
            val musicState = musicController.musicState.value
            if (musicState.isPlaying) {
                val current = musicController.getCurrentPosition()
                val duration = musicController.getDuration()
                if (duration > 0) {
                    emit(current.toFloat() / duration.toFloat())
                } else {
                    emit(0f)
                }
            } else {
                 // Emit current progress even if paused, just once per loop (or handle differently)
                 // Logic: if paused, progress doesn't change unless seeked.
                 // But for simplicity we keep emitting.
                 val current = musicController.getCurrentPosition()
                 val duration = musicController.getDuration()
                 if (duration > 0) {
                     emit(current.toFloat() / duration.toFloat())
                 }
            }
            delay(1000L) // Update every second
        }
    }
}
