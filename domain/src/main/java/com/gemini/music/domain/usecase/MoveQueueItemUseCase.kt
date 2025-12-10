package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicController
import javax.inject.Inject

class MoveQueueItemUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(fromIndex: Int, toIndex: Int) {
        musicController.moveSong(fromIndex, toIndex)
    }
}
