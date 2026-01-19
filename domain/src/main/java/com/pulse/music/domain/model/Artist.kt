package com.pulse.music.domain.model

data class Artist(
    val id: Long = 0,
    val name: String,
    val albumCount: Int = 0,
    val songCount: Int
)
