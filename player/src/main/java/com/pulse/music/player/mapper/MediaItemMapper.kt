package com.pulse.music.player.mapper

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.pulse.music.domain.model.Song

fun MediaItem.toSong(): Song {
    val meta = mediaMetadata
    return Song(
        id = mediaId.toLongOrNull() ?: 0L,
        title = meta.title?.toString() ?: "Unknown",
        artist = meta.artist?.toString() ?: "Unknown",
        album = meta.albumTitle?.toString() ?: "Unknown",
        albumId = meta.extras?.getLong("ALBUM_ID") ?: 0L,
        duration = meta.extras?.getLong("DURATION") ?: 0L,
        contentUri = requestMetadata.mediaUri.toString(),
        dataPath = meta.extras?.getString("DATA_PATH") ?: ""
    )
}

fun Song.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putString("DATA_PATH", dataPath)
        putLong("ALBUM_ID", albumId)
        putLong("DURATION", duration)
    }

    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setExtras(extras)
        .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(contentUri)
        .setMediaMetadata(metadata)
        .build()
}
