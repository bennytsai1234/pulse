package com.gemini.music.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val minAudioDuration: Flow<Long> = dataStore.data.map { preferences ->
        preferences[MIN_AUDIO_DURATION] ?: 10000L // Default 10s
    }

    val includedFolders: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[INCLUDED_FOLDERS] ?: emptySet()
    }
    
    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: THEME_SYSTEM
    }

    suspend fun setMinAudioDuration(durationMs: Long) {
        dataStore.edit { preferences ->
            preferences[MIN_AUDIO_DURATION] = durationMs
        }
    }

    suspend fun setIncludedFolders(folders: Set<String>) {
        dataStore.edit { preferences ->
            preferences[INCLUDED_FOLDERS] = folders
        }
    }
    
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    companion object {
        private val MIN_AUDIO_DURATION = longPreferencesKey("min_audio_duration")
        private val INCLUDED_FOLDERS = stringSetPreferencesKey("included_folders")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        
        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }
}
