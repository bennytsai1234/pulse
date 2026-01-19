package com.pulse.music.domain.repository

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
    val loudnessEnabled: Flow<Boolean>
    val loudnessGain: Flow<Int>

    // Playback Settings
    val playbackSpeed: Flow<Float>
    val crossfadeDuration: Flow<Int> // seconds
    val sleepTimerFadeOut: Flow<Boolean>
    val sleepTimerFadeDuration: Flow<Int> // seconds

    // Playback State Persistence
    val lastPlayedMediaId: Flow<String>
    val lastPlayedPosition: Flow<Long>
    val lastQueueMediaIds: Flow<List<String>> // Stored as comma-separated or JSON
    val lastQueueIndex: Flow<Int>

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
    suspend fun setLoudnessEnabled(enabled: Boolean)
    suspend fun setLoudnessGain(gain: Int)

    // Playback Settings
    suspend fun setPlaybackSpeed(speed: Float)
    suspend fun setCrossfadeDuration(seconds: Int)
    suspend fun setSleepTimerFadeOut(enabled: Boolean)
    suspend fun setSleepTimerFadeDuration(seconds: Int)

    // Playback State Persistence
    suspend fun setLastPlayedMediaId(mediaId: String)
    suspend fun setLastPlayedPosition(position: Long)
    suspend fun setLastQueueMediaIds(mediaIds: List<String>)
    suspend fun setLastQueueIndex(index: Int)

    // Theme Settings
    val useDynamicColor: Flow<Boolean>
    suspend fun setUseDynamicColor(use: Boolean)

    // Network Settings
    val connectTimeout: Flow<Long> // ms
    val readTimeout: Flow<Long> // ms
    val userAgent: Flow<String>
    
    // Display Settings
    val keepScreenOn: Flow<Boolean>
    val isRotationLocked: Flow<Boolean>
    
    // Library Settings
    val excludedFolders: Flow<Set<String>>
    
    suspend fun setConnectTimeout(timeout: Long)
    suspend fun setReadTimeout(timeout: Long)
    suspend fun setUserAgent(userAgent: String)
    
    suspend fun setKeepScreenOn(enabled: Boolean)
    suspend fun setRotationLocked(locked: Boolean)
    
    suspend fun setExcludedFolders(folders: Set<String>)

    companion object {
        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }
}


