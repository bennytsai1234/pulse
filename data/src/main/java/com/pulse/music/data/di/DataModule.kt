package com.pulse.music.data.di

import com.pulse.music.data.repository.MusicRepositoryImpl
import com.pulse.music.domain.repository.MusicRepository
import com.pulse.music.data.repository.LyricsRepositoryImpl
import com.pulse.music.domain.repository.LyricsRepository
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
        waveformRepositoryImpl: com.pulse.music.data.repository.WaveformRepositoryImpl
    ): com.pulse.music.domain.repository.WaveformRepository

    @Binds
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: com.pulse.music.data.repository.UserPreferencesRepositoryImpl
    ): com.pulse.music.domain.repository.UserPreferencesRepository

    @Binds
    abstract fun bindSearchRepository(
        searchRepositoryImpl: com.pulse.music.data.repository.SearchRepositoryImpl
    ): com.pulse.music.domain.repository.SearchRepository

    @Binds
    abstract fun bindEqPresetRepository(
        eqPresetRepositoryImpl: com.pulse.music.data.repository.EqPresetRepositoryImpl
    ): com.pulse.music.domain.repository.EqPresetRepository

    @Binds
    abstract fun bindListeningStatsRepository(
        listeningStatsRepositoryImpl: com.pulse.music.data.repository.ListeningStatsRepositoryImpl
    ): com.pulse.music.domain.repository.ListeningStatsRepository

    @Binds
    abstract fun bindFolderRepository(
        folderRepositoryImpl: com.pulse.music.data.repository.FolderRepositoryImpl
    ): com.pulse.music.domain.repository.FolderRepository

    @Binds
    abstract fun bindScrobbleRepository(
        scrobbleRepositoryImpl: com.pulse.music.data.repository.ScrobbleRepositoryImpl
    ): com.pulse.music.domain.repository.ScrobbleRepository

    @Binds
    abstract fun bindRecommendationRepository(
        recommendationRepositoryImpl: com.pulse.music.data.repository.RecommendationRepositoryImpl
    ): com.pulse.music.domain.repository.RecommendationRepository

    @Binds
    abstract fun bindLyricsEditRepository(
        lyricsEditRepositoryImpl: com.pulse.music.data.repository.LyricsEditRepositoryImpl
    ): com.pulse.music.domain.repository.LyricsEditRepository

    @Binds
    abstract fun bindBackupRepository(
        backupRepositoryImpl: com.pulse.music.data.repository.BackupRepositoryImpl
    ): com.pulse.music.domain.repository.BackupRepository

    @Binds
    abstract fun bindGoogleAuthProvider(
        googleDriveService: com.pulse.music.data.source.GoogleDriveService
    ): com.pulse.music.core.common.auth.GoogleAuthProvider

    @Binds
    abstract fun bindThemeRepository(
        themeRepositoryImpl: com.pulse.music.data.repository.ThemeRepositoryImpl
    ): com.pulse.music.domain.repository.ThemeRepository

    @Binds
    abstract fun bindDrivingModeRepository(
        drivingModeRepositoryImpl: com.pulse.music.data.repository.DrivingModeRepositoryImpl
    ): com.pulse.music.domain.repository.DrivingModeRepository

    @Binds
    abstract fun bindSyncRepository(
        syncRepositoryImpl: com.pulse.music.data.repository.SyncRepositoryImpl
    ): com.pulse.music.domain.repository.SyncRepository

    @Binds
    abstract fun bindQueueRepository(
        queueRepositoryImpl: com.pulse.music.data.repository.QueueRepositoryImpl
    ): com.pulse.music.domain.repository.QueueRepository

    @Binds
    abstract fun bindCrossfadeSettingsRepository(
        crossfadeSettingsRepositoryImpl: com.pulse.music.data.repository.CrossfadeSettingsRepositoryImpl
    ): com.pulse.music.domain.repository.CrossfadeSettingsRepository
}



