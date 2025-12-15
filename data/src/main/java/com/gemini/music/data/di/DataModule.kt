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

    @Binds
    abstract fun bindSearchRepository(
        searchRepositoryImpl: com.gemini.music.data.repository.SearchRepositoryImpl
    ): com.gemini.music.domain.repository.SearchRepository
    
    @Binds
    abstract fun bindEqPresetRepository(
        eqPresetRepositoryImpl: com.gemini.music.data.repository.EqPresetRepositoryImpl
    ): com.gemini.music.domain.repository.EqPresetRepository
    
    @Binds
    abstract fun bindListeningStatsRepository(
        listeningStatsRepositoryImpl: com.gemini.music.data.repository.ListeningStatsRepositoryImpl
    ): com.gemini.music.domain.repository.ListeningStatsRepository
    
    @Binds
    abstract fun bindFolderRepository(
        folderRepositoryImpl: com.gemini.music.data.repository.FolderRepositoryImpl
    ): com.gemini.music.domain.repository.FolderRepository
    
    @Binds
    abstract fun bindScrobbleRepository(
        scrobbleRepositoryImpl: com.gemini.music.data.repository.ScrobbleRepositoryImpl
    ): com.gemini.music.domain.repository.ScrobbleRepository
    
    @Binds
    abstract fun bindRecommendationRepository(
        recommendationRepositoryImpl: com.gemini.music.data.repository.RecommendationRepositoryImpl
    ): com.gemini.music.domain.repository.RecommendationRepository
    
    @Binds
    abstract fun bindLyricsEditRepository(
        lyricsEditRepositoryImpl: com.gemini.music.data.repository.LyricsEditRepositoryImpl
    ): com.gemini.music.domain.repository.LyricsEditRepository
}



