package com.pulse.music.player.di

import com.pulse.music.domain.repository.AudioEffectController
import com.pulse.music.player.controller.AudioEffectControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioEffectModule {

    @Binds
    @Singleton
    abstract fun bindAudioEffectController(
        audioEffectControllerImpl: AudioEffectControllerImpl
    ): AudioEffectController
}
