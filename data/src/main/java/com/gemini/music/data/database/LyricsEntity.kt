package com.gemini.music.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching lyrics fetched from network.
 * Uses songId as primary key since one song has one set of lyrics.
 */
@Entity(tableName = "lyrics_cache")
data class LyricsEntity(
    @PrimaryKey
    val songId: Long,
    val lyricsContent: String,
    val isSynced: Boolean,  // true if synced lyrics (with timestamps), false if plain
    val fetchedAt: Long = System.currentTimeMillis()
)
