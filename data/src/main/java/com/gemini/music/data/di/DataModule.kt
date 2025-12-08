package com.gemini.music.data.di

import com.gemini.music.data.repository.MusicRepositoryImpl
import com.gemini.music.domain.repository.MusicRepository
import com.gemini.music.data.repository.LyricsRepositoryImpl
import com.gemini.music.domain.repository.LyricsRepository
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

    @Binds
    abstract fun bindWaveformRepository(
        waveformRepositoryImpl: com.gemini.music.data.repository.WaveformRepositoryImpl
    ): com.gemini.music.domain.repository.WaveformRepository

    @Binds
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: com.gemini.music.data.repository.UserPreferencesRepositoryImpl
    ): com.gemini.music.domain.repository.UserPreferencesRepository
}
