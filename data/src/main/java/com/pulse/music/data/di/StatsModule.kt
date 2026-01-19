package com.pulse.music.data.di

import com.pulse.music.data.repository.StatsRepositoryImpl
import com.pulse.music.domain.repository.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StatsModule {

    @Binds
    @Singleton
    abstract fun bindStatsRepository(
        statsRepositoryImpl: StatsRepositoryImpl
    ): StatsRepository
}
