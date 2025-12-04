package com.sigma.music.domain.usecase

import com.sigma.music.domain.repository.MusicController
import javax.inject.Inject

class ToggleShuffleUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke() {
        musicController.toggleShuffle()
    }
}
