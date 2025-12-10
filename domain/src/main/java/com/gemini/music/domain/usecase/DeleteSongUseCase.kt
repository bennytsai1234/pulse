package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
import javax.inject.Inject

class DeleteSongUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(song: Song) {
        musicRepository.deleteSong(song)
    }
}
