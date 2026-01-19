package com.pulse.music.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pulse.music.domain.model.Artist

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val albumCount: Int,
    val songCount: Int
)

fun ArtistEntity.asDomainModel(): Artist {
    return Artist(
        id = id,
        name = name,
        albumCount = albumCount,
        songCount = songCount
    )
}

fun Artist.asEntity(): ArtistEntity {
    return ArtistEntity(
        id = id,
        name = name,
        albumCount = albumCount,
        songCount = songCount
    )
}
