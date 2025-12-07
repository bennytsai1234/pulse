package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicController
import javax.inject.Inject

class SkipToNextUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke() {
        musicController.skipToNext()
    }
}
