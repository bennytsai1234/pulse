package com.gemini.music.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val coverArtUri: String? = null
)
