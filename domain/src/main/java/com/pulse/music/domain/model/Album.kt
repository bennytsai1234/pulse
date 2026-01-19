package com.pulse.music.domain.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long = 0,
    val year: Int = 0,
    val songCount: Int,
    val albumArtUri: String? = null
) {
    val artUri: String
        get() = albumArtUri ?: "content://media/external/audio/albumart/$id"
}
