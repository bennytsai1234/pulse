package com.pulse.music.data.di

import android.content.Context
import androidx.room.Room
import com.pulse.music.data.database.PulseDatabase
import com.pulse.music.data.database.PlaylistDao
import com.pulse.music.data.database.SongDao
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
    fun provideDatabase(@ApplicationContext context: Context): PulseDatabase {
        return Room.databaseBuilder(
            context,
            PulseDatabase::class.java,
            "PULSE_music.db"
        )
        .fallbackToDestructiveMigration() // For development only, since we bump version
        .build()
    }

    @Provides
    fun provideSongDao(database: PulseDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    fun providePlaylistDao(database: PulseDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideFavoriteDao(database: PulseDatabase): com.pulse.music.data.database.FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun provideLyricsDao(database: PulseDatabase): com.pulse.music.data.database.LyricsDao {
        return database.lyricsDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: PulseDatabase): com.pulse.music.data.database.SearchHistoryDao {
        return database.searchHistoryDao()
    }
    
    @Provides
    fun provideCustomEqPresetDao(database: PulseDatabase): com.pulse.music.data.database.CustomEqPresetDao {
        return database.customEqPresetDao()
    }
    
    @Provides
    fun provideScrobbleDao(database: PulseDatabase): com.pulse.music.data.database.ScrobbleDao {
        return database.scrobbleDao()
    }
    
    @Provides
    fun provideCustomLyricsDao(database: PulseDatabase): com.pulse.music.data.database.CustomLyricsDao {
        return database.customLyricsDao()
    }

    @Provides
    fun provideAlbumDao(database: PulseDatabase): com.pulse.music.data.database.AlbumDao {
        return database.albumDao()
    }
    @Provides
    fun provideArtistDao(database: PulseDatabase): com.pulse.music.data.database.ArtistDao {
        return database.artistDao()
    }

    @Provides
    fun providePlaybackHistoryDao(database: PulseDatabase): com.pulse.music.data.database.PlaybackHistoryDao {
        return database.playbackHistoryDao()
    }
}


