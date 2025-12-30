package com.gemini.music.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gemini.music.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private val dataStore = context.dataStore

    override val minAudioDuration: Flow<Long> = dataStore.data.map { preferences ->
        preferences[MIN_AUDIO_DURATION] ?: 10000L
    }

    override val includedFolders: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[INCLUDED_FOLDERS] ?: emptySet()
    }

    override val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: UserPreferencesRepository.THEME_SYSTEM
    }

    override val useInternalEqualizer: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_INTERNAL_EQUALIZER] ?: false
    }

    // ==================== Equalizer Settings ====================

    override val equalizerEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[EQ_ENABLED] ?: false
    }

    override val equalizerBandLevels: Flow<List<Int>> = dataStore.data.map { preferences ->
        preferences[EQ_BAND_LEVELS]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }

    override val equalizerPresetIndex: Flow<Int> = dataStore.data.map { preferences ->
        preferences[EQ_PRESET_INDEX] ?: -1
    }

    override val bassBoostEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BASS_BOOST_ENABLED] ?: false
    }

    override val bassBoostStrength: Flow<Int> = dataStore.data.map { preferences ->
        preferences[BASS_BOOST_STRENGTH] ?: 0
    }

    override val virtualizerEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[VIRTUALIZER_ENABLED] ?: false
    }

    override val virtualizerStrength: Flow<Int> = dataStore.data.map { preferences ->
        preferences[VIRTUALIZER_STRENGTH] ?: 0
    }

    // ==================== Playback Settings ====================

    override val playbackSpeed: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PLAYBACK_SPEED] ?: 1.0f
    }

    override val crossfadeDuration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[CROSSFADE_DURATION] ?: 0
    }

    override val sleepTimerFadeOut: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SLEEP_TIMER_FADE_OUT] ?: true
    }

    override val sleepTimerFadeDuration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SLEEP_TIMER_FADE_DURATION] ?: 30
    }

    // ==================== Playback State Persistence ====================

    override val lastPlayedMediaId: Flow<String> = dataStore.data.map { preferences ->
        preferences[LAST_PLAYED_MEDIA_ID] ?: ""
    }

    override val lastPlayedPosition: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_PLAYED_POSITION] ?: 0L
    }

    override val lastQueueMediaIds: Flow<List<String>> = dataStore.data.map { preferences ->
        preferences[LAST_QUEUE_MEDIA_IDS]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    override val lastQueueIndex: Flow<Int> = dataStore.data.map { preferences ->
        preferences[LAST_QUEUE_INDEX] ?: 0
    }

    override suspend fun setMinAudioDuration(durationMs: Long) {
        dataStore.edit { preferences ->
            preferences[MIN_AUDIO_DURATION] = durationMs
        }
    }

    override suspend fun setIncludedFolders(folders: Set<String>) {
        dataStore.edit { preferences ->
            preferences[INCLUDED_FOLDERS] = folders
        }
    }

    override suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    override suspend fun setUseInternalEqualizer(useInternal: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_INTERNAL_EQUALIZER] = useInternal
        }
    }

    // ==================== Equalizer Settings ====================

    override suspend fun setEqualizerEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[EQ_ENABLED] = enabled
        }
    }

    override suspend fun setEqualizerBandLevels(levels: List<Int>) {
        dataStore.edit { preferences ->
            preferences[EQ_BAND_LEVELS] = levels.joinToString(",")
        }
    }

    override suspend fun setEqualizerPresetIndex(index: Int) {
        dataStore.edit { preferences ->
            preferences[EQ_PRESET_INDEX] = index
        }
    }

    override suspend fun setBassBoostEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BASS_BOOST_ENABLED] = enabled
        }
    }

    override suspend fun setBassBoostStrength(strength: Int) {
        dataStore.edit { preferences ->
            preferences[BASS_BOOST_STRENGTH] = strength
        }
    }

    override suspend fun setVirtualizerEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIRTUALIZER_ENABLED] = enabled
        }
    }

    override suspend fun setVirtualizerStrength(strength: Int) {
        dataStore.edit { preferences ->
            preferences[VIRTUALIZER_STRENGTH] = strength
        }
    }

    // ==================== Playback Settings ====================

    override suspend fun setPlaybackSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[PLAYBACK_SPEED] = speed.coerceIn(0.5f, 2.0f)
        }
    }

    override suspend fun setCrossfadeDuration(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[CROSSFADE_DURATION] = seconds.coerceIn(0, 12)
        }
    }

    override suspend fun setSleepTimerFadeOut(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SLEEP_TIMER_FADE_OUT] = enabled
        }
    }

    override suspend fun setSleepTimerFadeDuration(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[SLEEP_TIMER_FADE_DURATION] = seconds.coerceIn(5, 60)
        }
    }

    // ==================== Playback State Persistence ====================

    override suspend fun setLastPlayedMediaId(mediaId: String) {
        dataStore.edit { preferences ->
            preferences[LAST_PLAYED_MEDIA_ID] = mediaId
        }
    }

    override suspend fun setLastPlayedPosition(position: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_PLAYED_POSITION] = position
        }
    }

    override suspend fun setLastQueueMediaIds(mediaIds: List<String>) {
        dataStore.edit { preferences ->
            preferences[LAST_QUEUE_MEDIA_IDS] = mediaIds.joinToString(",")
        }
    }

    override suspend fun setLastQueueIndex(index: Int) {
        dataStore.edit { preferences ->
            preferences[LAST_QUEUE_INDEX] = index
        }
    }

    companion object {
        private val MIN_AUDIO_DURATION = longPreferencesKey("min_audio_duration")
        private val INCLUDED_FOLDERS = stringSetPreferencesKey("included_folders")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val USE_INTERNAL_EQUALIZER = booleanPreferencesKey("use_internal_equalizer")

        // Equalizer
        private val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        private val EQ_BAND_LEVELS = stringPreferencesKey("eq_band_levels")
        private val EQ_PRESET_INDEX = intPreferencesKey("eq_preset_index")
        private val BASS_BOOST_ENABLED = booleanPreferencesKey("bass_boost_enabled")
        private val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")
        private val VIRTUALIZER_ENABLED = booleanPreferencesKey("virtualizer_enabled")
        private val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")

        // Playback
        private val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        private val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        private val SLEEP_TIMER_FADE_OUT = booleanPreferencesKey("sleep_timer_fade_out")
        private val SLEEP_TIMER_FADE_DURATION = intPreferencesKey("sleep_timer_fade_duration")

        // Playback State
        private val LAST_PLAYED_MEDIA_ID = stringPreferencesKey("last_played_media_id")
        private val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
        private val LAST_QUEUE_MEDIA_IDS = stringPreferencesKey("last_queue_media_ids")
        private val LAST_QUEUE_INDEX = intPreferencesKey("last_queue_index")
    }
}


