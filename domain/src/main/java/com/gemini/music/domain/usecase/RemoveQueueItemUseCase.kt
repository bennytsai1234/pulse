package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicController
import javax.inject.Inject

class RemoveQueueItemUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(index: Int) {
        musicController.removeSong(index)
    }
}
