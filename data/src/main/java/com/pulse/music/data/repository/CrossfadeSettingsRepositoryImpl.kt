package com.pulse.music.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pulse.music.domain.model.CrossfadeCurve
import com.pulse.music.domain.model.CrossfadeSettings
import com.pulse.music.domain.repository.CrossfadeSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.crossfadeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "crossfade_settings"
)

/**
 * 交叉淡入淡出設定 Repository 實作
 * 使用 DataStore 進行設定持久化
 */
@Singleton
class CrossfadeSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CrossfadeSettingsRepository {

    private val dataStore = context.crossfadeDataStore

    // ==================== Individual Flows ====================

    override val enabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ENABLED] ?: false
    }

    override val durationMs: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_DURATION_MS] ?: CrossfadeSettings.DEFAULT_DURATION_MS
    }

    override val curve: Flow<CrossfadeCurve> = dataStore.data.map { preferences ->
        CrossfadeCurve.fromOrdinal(preferences[KEY_CURVE_ORDINAL] ?: 0)
    }

    override val applyOnManualSkip: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_APPLY_ON_MANUAL_SKIP] ?: true
    }

    override val albumContinuous: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ALBUM_CONTINUOUS] ?: true
    }

    override val silenceDetection: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_SILENCE_DETECTION] ?: false
    }

    private val silenceThresholdDb: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_SILENCE_THRESHOLD_DB] ?: -45f
    }

    // ==================== Combined Settings Flow ====================

    override val crossfadeSettings: Flow<CrossfadeSettings> = combine(
        enabled,
        durationMs,
        curve,
        applyOnManualSkip,
        albumContinuous,
        silenceDetection
    ) { flows ->
        CrossfadeSettings(
            enabled = flows[0] as Boolean,
            durationMs = flows[1] as Int,
            curve = flows[2] as CrossfadeCurve,
            applyOnManualSkip = flows[3] as Boolean,
            albumContinuous = flows[4] as Boolean,
            silenceDetection = flows[5] as Boolean
        )
    }

    // ==================== Setters ====================

    override suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ENABLED] = enabled
        }
    }

    override suspend fun setDurationMs(durationMs: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_DURATION_MS] = durationMs.coerceIn(
                CrossfadeSettings.MIN_DURATION_MS,
                CrossfadeSettings.MAX_DURATION_MS
            )
        }
    }

    override suspend fun setCurve(curve: CrossfadeCurve) {
        dataStore.edit { preferences ->
            preferences[KEY_CURVE_ORDINAL] = curve.ordinal
        }
    }

    override suspend fun setApplyOnManualSkip(apply: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_APPLY_ON_MANUAL_SKIP] = apply
        }
    }

    override suspend fun setAlbumContinuous(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ALBUM_CONTINUOUS] = enabled
        }
    }

    override suspend fun setSilenceDetection(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SILENCE_DETECTION] = enabled
        }
    }

    override suspend fun updateSettings(settings: CrossfadeSettings) {
        dataStore.edit { preferences ->
            preferences[KEY_ENABLED] = settings.enabled
            preferences[KEY_DURATION_MS] = settings.durationMs.coerceIn(
                CrossfadeSettings.MIN_DURATION_MS,
                CrossfadeSettings.MAX_DURATION_MS
            )
            preferences[KEY_CURVE_ORDINAL] = settings.curve.ordinal
            preferences[KEY_APPLY_ON_MANUAL_SKIP] = settings.applyOnManualSkip
            preferences[KEY_ALBUM_CONTINUOUS] = settings.albumContinuous
            preferences[KEY_SILENCE_DETECTION] = settings.silenceDetection
            preferences[KEY_SILENCE_THRESHOLD_DB] = settings.silenceThresholdDb
        }
    }

    companion object {
        private val KEY_ENABLED = booleanPreferencesKey("crossfade_enabled")
        private val KEY_DURATION_MS = intPreferencesKey("crossfade_duration_ms")
        private val KEY_CURVE_ORDINAL = intPreferencesKey("crossfade_curve_ordinal")
        private val KEY_APPLY_ON_MANUAL_SKIP = booleanPreferencesKey("crossfade_apply_on_manual_skip")
        private val KEY_ALBUM_CONTINUOUS = booleanPreferencesKey("crossfade_album_continuous")
        private val KEY_SILENCE_DETECTION = booleanPreferencesKey("crossfade_silence_detection")
        private val KEY_SILENCE_THRESHOLD_DB = floatPreferencesKey("crossfade_silence_threshold_db")
    }
}
