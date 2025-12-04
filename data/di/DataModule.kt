package com.sigma.music.data.di

import com.sigma.music.data.repository.MusicRepositoryImpl
import com.sigma.music.domain.repository.MusicRepository
import com.sigma.music.data.repository.LyricsRepositoryImpl
import com.sigma.music.domain.repository.LyricsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    abstract fun bindLyricsRepository(
        lyricsRepositoryImpl: LyricsRepositoryImpl
    ): LyricsRepository
}
