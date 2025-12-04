package com.sigma.music.domain.usecase

import com.sigma.music.domain.model.MusicState
import com.sigma.music.domain.repository.MusicController
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetMusicStateUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(): StateFlow<MusicState> {
        return musicController.musicState
    }
}
