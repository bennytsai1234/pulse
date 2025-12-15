package com.gemini.music.domain.usecase.lyrics

import com.gemini.music.domain.repository.LyricsEditRepository
import javax.inject.Inject

/**
 * 匯入歌詞的 UseCase。
 * 支持從 LRC 檔案匯入歌詞。
 */
class ImportLyricsUseCase @Inject constructor(
    private val lyricsEditRepository: LyricsEditRepository
) {
    /**
     * 從 LRC 內容匯入歌詞。
     * @param songId 目標歌曲 ID
     * @param lrcContent LRC 檔案內容
     * @return 是否匯入成功
     */
    suspend operator fun invoke(songId: Long, lrcContent: String): Boolean {
        return lyricsEditRepository.importFromLrc(songId, lrcContent)
    }
}
