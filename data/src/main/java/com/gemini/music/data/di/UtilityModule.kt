package com.gemini.music.data.di

import android.content.Context
import com.gemini.music.data.source.AudioWaveformExtractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {

    @Provides
    @Singleton
    fun provideAudioWaveformExtractor(
        @ApplicationContext context: Context
    ): AudioWaveformExtractor {
        return AudioWaveformExtractor(context)
    }
}
