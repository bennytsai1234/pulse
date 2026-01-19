package com.pulse.music.domain.usecase.audio

import com.pulse.music.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetAudioBalanceUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    /**
     * Set left/right balance.
     * @param balance Float value from -1.0 (Left) to 1.0 (Right). 0.0 is center.
     */
    suspend operator fun invoke(balance: Float) {
        // Validation
        val clampedBalance = balance.coerceIn(-1.0f, 1.0f)
        // Store in prefs (Assuming repo support, if not need to add it)
        // For now, let's assume we need to add 'setAudioBalance' to repo.
        // Waiting for verification of Repo update.
    }
}
