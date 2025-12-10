package com.gemini.music.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SongEntity::class, PlaylistEntity::class, PlaylistSongCrossRef::class, FavoriteEntity::class, LyricsEntity::class], 
    version = 4, 
    exportSchema = false
)
abstract class GeminiDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun lyricsDao(): LyricsDao
}
