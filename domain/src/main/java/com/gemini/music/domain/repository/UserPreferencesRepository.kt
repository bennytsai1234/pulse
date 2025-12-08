package com.gemini.music.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val minAudioDuration: Flow<Long>
    val includedFolders: Flow<Set<String>>
    val themeMode: Flow<String>

    suspend fun setMinAudioDuration(durationMs: Long)
    suspend fun setIncludedFolders(folders: Set<String>)
    suspend fun setThemeMode(mode: String)

    companion object {
        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }
}
