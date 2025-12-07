package com.gemini.music.domain.model

/**
 * 代表一首單曲的核心業務模型。
 * 這是整個 App 中流通的標準資料格式。
 */
data class Song(
    val id: Long,              // MediaStore ID
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,         // 用於查詢專輯封面
    val duration: Long,        // 毫秒
    val contentUri: String,    // 實際檔案的 Uri (content://...)
    val dataPath: String,      // 檔案絕對路徑 (用於查找歌詞)
    val trackNumber: Int = 0,
    val year: Int = 0,
    val dateAdded: Long = 0    // 用於「最近加入」排序
) {
    // 輔助屬性：產生專輯封面 Uri (用於 Coil)
    val albumArtUri: String
        get() = "content://media/external/audio/albumart/$albumId"
}
