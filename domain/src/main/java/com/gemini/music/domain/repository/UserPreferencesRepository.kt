package com.gemini.music.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val minAudioDuration: Flow<Long>
    val includedFolders: Flow<Set<String>>
    val themeMode: Flow<String>
    val useInternalEqualizer: Flow<Boolean>
    
    // Equalizer Settings
    val equalizerEnabled: Flow<Boolean>
    val equalizerBandLevels: Flow<List<Int>>
    val equalizerPresetIndex: Flow<Int>
    val bassBoostEnabled: Flow<Boolean>
    val bassBoostStrength: Flow<Int>
    val virtualizerEnabled: Flow<Boolean>
    val virtualizerStrength: Flow<Int>
    
    // Playback Settings
    val playbackSpeed: Flow<Float>
    val crossfadeDuration: Flow<Int> // seconds
    val sleepTimerFadeOut: Flow<Boolean>
    val sleepTimerFadeDuration: Flow<Int> // seconds

    suspend fun setMinAudioDuration(durationMs: Long)
    suspend fun setIncludedFolders(folders: Set<String>)
    suspend fun setThemeMode(mode: String)
    suspend fun setUseInternalEqualizer(useInternal: Boolean)
    
    // Equalizer Settings
    suspend fun setEqualizerEnabled(enabled: Boolean)
    suspend fun setEqualizerBandLevels(levels: List<Int>)
    suspend fun setEqualizerPresetIndex(index: Int)
    suspend fun setBassBoostEnabled(enabled: Boolean)
    suspend fun setBassBoostStrength(strength: Int)
    suspend fun setVirtualizerEnabled(enabled: Boolean)
    suspend fun setVirtualizerStrength(strength: Int)
    
    // Playback Settings
    suspend fun setPlaybackSpeed(speed: Float)
    suspend fun setCrossfadeDuration(seconds: Int)
    suspend fun setSleepTimerFadeOut(enabled: Boolean)
    suspend fun setSleepTimerFadeDuration(seconds: Int)


    companion object {
        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }
}


