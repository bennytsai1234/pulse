package com.pulse.music.domain.usecase.audio

import com.pulse.music.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetBassBoostStrengthUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(strength: Int) {
        userPreferencesRepository.setBassBoostStrength(strength)
    }
}

class SetVirtualizerStrengthUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(strength: Int) {
        userPreferencesRepository.setVirtualizerStrength(strength)
    }
}
