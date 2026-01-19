package com.pulse.music.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class, 
        PlaylistEntity::class, 
        PlaylistSongCrossRef::class, 
        FavoriteEntity::class, 
        LyricsEntity::class, 
        SearchHistoryEntity::class,
        CustomEqPresetEntity::class,
        PlaybackHistoryEntity::class,
        ScrobbleEntity::class,
        CustomLyricsEntity::class,
        AlbumEntity::class,
        ArtistEntity::class
    ], 
    version = 10, 
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun customEqPresetDao(): CustomEqPresetDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun scrobbleDao(): ScrobbleDao
    abstract fun customLyricsDao(): CustomLyricsDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
}


