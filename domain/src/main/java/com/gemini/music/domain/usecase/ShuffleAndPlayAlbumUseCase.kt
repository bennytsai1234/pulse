package com.gemini.music.domain.usecase

import com.gemini.music.domain.repository.MusicController
import com.gemini.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ShuffleAndPlayAlbumUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicController: MusicController
) {
    suspend operator fun invoke(albumId: Long) {
        val songs = musicRepository.getSongsByAlbumId(albumId).first()
        // Simple shuffle implementation:
        // 1. Enable shuffle mode
        // 2. Play songs
        musicController.toggleShuffle()
        musicController.playSongs(songs, 0)
    }
}
