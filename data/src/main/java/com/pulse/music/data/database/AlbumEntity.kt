package com.pulse.music.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pulse.music.domain.model.Album

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long, // Could be useful if we have artist IDs
    val year: Int,
    val songCount: Int,
    val albumArtUri: String?
)

fun AlbumEntity.asDomainModel(): Album {
    return Album(
        id = id,
        title = title,
        artist = artist,
        artistId = artistId,
        year = year,
        songCount = songCount,
        albumArtUri = albumArtUri
    )
}

fun Album.asEntity(): AlbumEntity {
    return AlbumEntity(
        id = id,
        title = title,
        artist = artist,
        artistId = artistId,
        year = year,
        songCount = songCount,
        albumArtUri = albumArtUri
    )
}
