package com.pulse.music.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pulse.music.domain.model.AppTheme
import com.pulse.music.domain.model.CustomTheme
import com.pulse.music.domain.model.ThemeMode
import com.pulse.music.domain.model.ThemePalette
import com.pulse.music.domain.model.ThemeSettings
import com.pulse.music.domain.repository.ThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ThemeRepository {
    
    private val dataStore = context.themeDataStore
    private val json = Json { ignoreUnknownKeys = true }
    
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val SELECTED_PALETTE = stringPreferencesKey("selected_palette")
        val CUSTOM_THEME_ID = stringPreferencesKey("custom_theme_id")
        val USE_AMOLED_BLACK = booleanPreferencesKey("use_amoled_black")
        val CONTRAST_LEVEL = floatPreferencesKey("contrast_level")
        val CUSTOM_THEMES_JSON = stringPreferencesKey("custom_themes_json")
    }
    
    override fun observeThemeSettings(): Flow<ThemeSettings> {
        return dataStore.data.map { preferences ->
            ThemeSettings(
                themeMode = preferences[PreferencesKeys.THEME_MODE]?.let { 
                    ThemeMode.valueOf(it) 
                } ?: ThemeMode.SYSTEM,
                useDynamicColor = preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: false,
                selectedPalette = preferences[PreferencesKeys.SELECTED_PALETTE]?.let {
                    ThemePalette.valueOf(it)
                } ?: ThemePalette.PULSE,
                customThemeId = preferences[PreferencesKeys.CUSTOM_THEME_ID],
                useAmoledBlack = preferences[PreferencesKeys.USE_AMOLED_BLACK] ?: false,
                contrastLevel = preferences[PreferencesKeys.CONTRAST_LEVEL] ?: 1.0f
            )
        }
    }
    
    override fun observeAppTheme(): Flow<AppTheme> {
        return observeThemeSettings().map { settings ->
            val isDarkMode = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK, ThemeMode.AMOLED -> true
                ThemeMode.SYSTEM -> true // 預設暗色
            }
            
            val palette = settings.selectedPalette
            val useAmoled = settings.useAmoledBlack || settings.themeMode == ThemeMode.AMOLED
            
            AppTheme(
                settings = settings,
                isDarkMode = isDarkMode,
                primaryColor = palette.primaryColor,
                accentColor = palette.accentColor,
                backgroundColor = if (useAmoled) 0xFF000000 else 0xFF121212,
                surfaceColor = if (useAmoled) 0xFF0A0A0A else 0xFF1E1E1E,
                textPrimaryColor = 0xFFFFFFFF,
                textSecondaryColor = 0xB3FFFFFF
            )
        }
    }
    
    override suspend fun updateThemeSettings(settings: ThemeSettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = settings.themeMode.name
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] = settings.useDynamicColor
            preferences[PreferencesKeys.SELECTED_PALETTE] = settings.selectedPalette.name
            preferences[PreferencesKeys.CUSTOM_THEME_ID] = settings.customThemeId ?: ""
            preferences[PreferencesKeys.USE_AMOLED_BLACK] = settings.useAmoledBlack
            preferences[PreferencesKeys.CONTRAST_LEVEL] = settings.contrastLevel
        }
    }
    
    override suspend fun setThemePalette(palette: ThemePalette) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PALETTE] = palette.name
            preferences[PreferencesKeys.CUSTOM_THEME_ID] = ""
        }
    }
    
    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] = enabled
        }
    }
    
    override suspend fun setAmoledBlackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_AMOLED_BLACK] = enabled
        }
    }
    
    override fun observeCustomThemes(): Flow<List<CustomTheme>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[PreferencesKeys.CUSTOM_THEMES_JSON] ?: "[]"
            try {
                json.decodeFromString<List<CustomTheme>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    override suspend fun saveCustomTheme(theme: CustomTheme) {
        val currentThemes = observeCustomThemes().first().toMutableList()
        val existingIndex = currentThemes.indexOfFirst { it.id == theme.id }
        
        if (existingIndex >= 0) {
            currentThemes[existingIndex] = theme
        } else {
            currentThemes.add(theme)
        }
        
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_THEMES_JSON] = json.encodeToString(currentThemes)
        }
    }
    
    override suspend fun deleteCustomTheme(themeId: String) {
        val currentThemes = observeCustomThemes().first().toMutableList()
        currentThemes.removeAll { it.id == themeId }
        
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_THEMES_JSON] = json.encodeToString(currentThemes)
            if (preferences[PreferencesKeys.CUSTOM_THEME_ID] == themeId) {
                preferences[PreferencesKeys.CUSTOM_THEME_ID] = ""
            }
        }
    }
    
    override suspend fun selectCustomTheme(themeId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_THEME_ID] = themeId
        }
    }
    
    override suspend fun getCurrentThemeSettings(): ThemeSettings {
        return observeThemeSettings().first()
    }
}
