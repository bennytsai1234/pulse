package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicController
import javax.inject.Inject

class PlayQueueItemUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(index: Int) {
        musicController.playSongAt(index)
    }
}
