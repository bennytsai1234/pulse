package com.gemini.music.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gemini.music.domain.model.Song

/**
 * Room Database Table: songs
 * 將 Domain Model 映射為資料庫儲存格式。
 */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val contentUri: String,
    val dataPath: String,
    val trackNumber: Int,
    val year: Int,
    val dateAdded: Long
)

// Extension function: Entity -> Domain Model
fun SongEntity.asDomainModel(): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        contentUri = contentUri,
        dataPath = dataPath,
        trackNumber = trackNumber,
        year = year,
        dateAdded = dateAdded
    )
}

// Extension function: Domain Model -> Entity
fun Song.asEntity(): SongEntity {
    return SongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        contentUri = contentUri,
        dataPath = dataPath,
        trackNumber = trackNumber,
        year = year,
        dateAdded = dateAdded
    )
}
