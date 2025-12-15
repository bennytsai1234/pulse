package com.gemini.music.domain.usecase.tageditor

import com.gemini.music.domain.model.SongTags
import com.gemini.music.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * 更新歌曲標籤資訊的 UseCase
 */
class UpdateSongTagsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(tags: SongTags): Boolean {
        return musicRepository.updateSongTags(tags)
    }
}
