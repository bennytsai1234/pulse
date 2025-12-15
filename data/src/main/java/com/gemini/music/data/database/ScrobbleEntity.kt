package com.gemini.music.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Scrobble 記錄實體。
 */
@Entity(tableName = "scrobbles")
data class ScrobbleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val timestamp: Long,
    val scrobbledAt: Long? = null,
    val status: String = "PENDING" // PENDING, SCROBBLED, FAILED, LOCAL_ONLY
)
