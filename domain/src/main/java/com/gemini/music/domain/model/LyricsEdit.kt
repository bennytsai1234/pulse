package com.gemini.music.domain.model

/**
 * 離線歌詞編輯資料模型。
 * 支持手動編輯和時間戳調整。
 */
data class LyricsEdit(
    val songId: Long,
    val lines: List<EditableLyricLine>,
    val offset: Long = 0, // 全局時間偏移 (毫秒)
    val source: LyricsSource = LyricsSource.USER_EDITED,
    val lastModified: Long = System.currentTimeMillis()
)

data class EditableLyricLine(
    val index: Int,
    val timestamp: Long, // 毫秒
    val text: String,
    val translatedText: String? = null
)

enum class LyricsSource {
    NETWORK,        // 從網路抓取
    EMBEDDED,       // 嵌入在音訊檔案中
    USER_EDITED,    // 用戶手動編輯
    IMPORTED        // 從 LRC 檔案匯入
}

/**
 * 歌詞同步調整事件。
 */
sealed class LyricsSyncEvent {
    data class AdjustOffset(val offsetMs: Long) : LyricsSyncEvent()
    data class AdjustLineTimestamp(val lineIndex: Int, val newTimestamp: Long) : LyricsSyncEvent()
    data class InsertLine(val afterIndex: Int, val line: EditableLyricLine) : LyricsSyncEvent()
    data class DeleteLine(val index: Int) : LyricsSyncEvent()
    data class UpdateLineText(val index: Int, val newText: String) : LyricsSyncEvent()
}
