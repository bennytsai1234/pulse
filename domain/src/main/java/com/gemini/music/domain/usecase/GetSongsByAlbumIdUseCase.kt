package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSongsByAlbumIdUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(albumId: Long): Flow<List<Song>> {
        return musicRepository.getSongsByAlbumId(albumId)
    }
}
