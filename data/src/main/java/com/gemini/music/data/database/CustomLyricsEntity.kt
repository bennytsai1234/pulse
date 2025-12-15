package com.gemini.music.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 自定義歌詞實體。
 */
@Entity(tableName = "custom_lyrics")
data class CustomLyricsEntity(
    @PrimaryKey
    val songId: Long,
    val lyricsJson: String, // JSON 格式的歌詞行列表
    val globalOffset: Long = 0,
    val source: String = "USER_EDITED", // NETWORK, EMBEDDED, USER_EDITED, IMPORTED
    val lastModified: Long = System.currentTimeMillis()
)
