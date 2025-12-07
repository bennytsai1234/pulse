package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicController
import javax.inject.Inject

class PlaySongUseCase @Inject constructor(
    private val musicController: MusicController
) {
    operator fun invoke(songs: List<Song>, index: Int) {
        musicController.playSongs(songs, index)
    }
}
