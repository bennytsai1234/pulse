package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicController
import com.gemini.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlayAlbumUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicController: MusicController
) {
    suspend operator fun invoke(albumId: Long) {
        val songs = musicRepository.getSongsByAlbumId(albumId).first()
        musicController.playSongs(songs, 0)
    }
}
