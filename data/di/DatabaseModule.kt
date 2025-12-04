package com.sigma.music.data.di

import android.content.Context
import androidx.room.Room
import com.sigma.music.data.database.SigmaDatabase
import com.sigma.music.data.database.SongDao
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
    fun provideDatabase(@ApplicationContext context: Context): SigmaDatabase {
        return Room.databaseBuilder(
            context,
            SigmaDatabase::class.java,
            "sigma_music.db"
        ).build()
    }

    @Provides
    fun provideSongDao(database: SigmaDatabase): SongDao {
        return database.songDao()
    }
}
