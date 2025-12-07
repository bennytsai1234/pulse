package com.gemini.music.domain.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int
) {
    val artUri: String
        get() = "content://media/external/audio/albumart/$id"
}
