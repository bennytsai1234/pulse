package com.sigma.music.domain.usecase

import com.sigma.music.domain.repository.MusicController
import javax.inject.Inject

class PlayQueueItemUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(index: Int) {
        musicController.playSongAt(index)
    }
}
