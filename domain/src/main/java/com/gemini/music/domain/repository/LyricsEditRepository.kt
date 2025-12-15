package com.gemini.music.domain.repository

import com.gemini.music.domain.model.EditableLyricLine
import com.gemini.music.domain.model.LyricsEdit
import com.gemini.music.domain.model.LyricsSyncEvent
import kotlinx.coroutines.flow.Flow

/**
 * 歌詞編輯資料倉庫介面。
 * 支持離線歌詞編輯、時間戳調整和匯入匯出。
 */
interface LyricsEditRepository {
    
    /**
     * 取得歌曲的可編輯歌詞。
     */
    fun getEditableLyrics(songId: Long): Flow<LyricsEdit?>
    
    /**
     * 儲存編輯後的歌詞。
     */
    suspend fun saveLyrics(lyricsEdit: LyricsEdit)
    
    /**
     * 應用歌詞同步事件。
     */
    suspend fun applySyncEvent(songId: Long, event: LyricsSyncEvent)
    
    /**
     * 調整全局時間偏移。
     */
    suspend fun adjustGlobalOffset(songId: Long, offsetMs: Long)
    
    /**
     * 從 LRC 檔案匯入歌詞。
     * @param songId 目標歌曲 ID
     * @param lrcContent LRC 檔案內容
     */
    suspend fun importFromLrc(songId: Long, lrcContent: String): Boolean
    
    /**
     * 匯出歌詞為 LRC 格式。
     */
    suspend fun exportToLrc(songId: Long): String?
    
    /**
     * 將歌詞嵌入到音訊檔案中。
     * 需要 TagEditor 支援。
     */
    suspend fun embedLyricsToFile(songId: Long): Boolean
    
    /**
     * 從音訊檔案中提取嵌入的歌詞。
     */
    suspend fun extractEmbeddedLyrics(songId: Long): LyricsEdit?
    
    /**
     * 刪除歌曲的自定義歌詞。
     */
    suspend fun deleteLyrics(songId: Long)
    
    /**
     * 檢查歌曲是否有自定義歌詞。
     */
    fun hasCustomLyrics(songId: Long): Flow<Boolean>
}
