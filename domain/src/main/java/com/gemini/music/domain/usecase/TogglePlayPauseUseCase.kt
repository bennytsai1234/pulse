package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicController
import javax.inject.Inject

class TogglePlayPauseUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke() {
        val currentState = musicController.musicState.value
        if (currentState.isPlaying) {
            musicController.pause()
        } else {
            // If we are paused but have songs in queue/current, resume.
            // If queue is empty, maybe do nothing or play distinct logic.
            // For now simple resume/pause.
            musicController.resume()
        }
    }
}
