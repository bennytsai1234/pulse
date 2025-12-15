package com.gemini.music.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gemini.music.domain.model.PlaybackRecord

/**
 * 播放記錄實體
 */
@Entity(
    tableName = "playback_history",
    indices = [
        Index(value = ["songId"]),
        Index(value = ["playedAt"]),
        Index(value = ["artistName"])
    ]
)
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val songTitle: String,
    val artistName: String,
    val albumName: String,
    val albumArtUri: String? = null,
    val playedAt: Long = System.currentTimeMillis(),
    val durationPlayed: Long = 0,
    val completed: Boolean = false
)

fun PlaybackHistoryEntity.asDomainModel() = PlaybackRecord(
    id = id,
    songId = songId,
    songTitle = songTitle,
    artistName = artistName,
    albumName = albumName,
    playedAt = playedAt,
    durationPlayed = durationPlayed,
    completed = completed
)

fun PlaybackRecord.asEntity(albumArtUri: String? = null) = PlaybackHistoryEntity(
    id = id,
    songId = songId,
    songTitle = songTitle,
    artistName = artistName,
    albumName = albumName,
    albumArtUri = albumArtUri,
    playedAt = playedAt,
    durationPlayed = durationPlayed,
    completed = completed
)
