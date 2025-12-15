package com.gemini.music.domain.model

/**
 * 歌曲標籤資料模型，用於 Tag Editor
 */
data class SongTags(
    val songId: Long,
    val filePath: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val genre: String,
    val year: String,
    val trackNumber: String,
    val discNumber: String,
    val comment: String
)
