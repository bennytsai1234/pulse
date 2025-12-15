package com.gemini.music.data.di

import android.content.Context
import androidx.room.Room
import com.gemini.music.data.database.GeminiDatabase
import com.gemini.music.data.database.PlaylistDao
import com.gemini.music.data.database.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GeminiDatabase {
        return Room.databaseBuilder(
            context,
            GeminiDatabase::class.java,
            "gemini_music.db"
        )
        .fallbackToDestructiveMigration() // For development only, since we bump version
        .build()
    }

    @Provides
    fun provideSongDao(database: GeminiDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    fun providePlaylistDao(database: GeminiDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideFavoriteDao(database: GeminiDatabase): com.gemini.music.data.database.FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun provideLyricsDao(database: GeminiDatabase): com.gemini.music.data.database.LyricsDao {
        return database.lyricsDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: GeminiDatabase): com.gemini.music.data.database.SearchHistoryDao {
        return database.searchHistoryDao()
    }
    
    @Provides
    fun provideCustomEqPresetDao(database: GeminiDatabase): com.gemini.music.data.database.CustomEqPresetDao {
        return database.customEqPresetDao()
    }
    
    @Provides
    fun providePlaybackHistoryDao(database: GeminiDatabase): com.gemini.music.data.database.PlaybackHistoryDao {
        return database.playbackHistoryDao()
    }
    
    @Provides
    fun provideScrobbleDao(database: GeminiDatabase): com.gemini.music.data.database.ScrobbleDao {
        return database.scrobbleDao()
    }
    
    @Provides
    fun provideCustomLyricsDao(database: GeminiDatabase): com.gemini.music.data.database.CustomLyricsDao {
        return database.customLyricsDao()
    }
}


