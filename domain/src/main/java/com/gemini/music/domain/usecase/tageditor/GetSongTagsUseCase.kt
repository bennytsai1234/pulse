package com.gemini.music.domain.usecase.tageditor

import com.gemini.music.domain.model.SongTags
import com.gemini.music.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * 取得歌曲標籤資訊的 UseCase
 */
class GetSongTagsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(songId: Long): SongTags? {
        return musicRepository.getSongTags(songId)
    }
}
