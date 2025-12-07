package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.MusicState
import com.gemini.music.domain.repository.MusicController
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetMusicStateUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(): StateFlow<MusicState> {
        return musicController.musicState
    }
}
