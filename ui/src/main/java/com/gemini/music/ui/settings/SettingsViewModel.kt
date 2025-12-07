package com.gemini.music.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.data.repository.UserPreferencesRepository
import com.gemini.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val minAudioDuration: Long = 10000L,
    val includedFolders: Set<String> = emptySet(),
    val themeMode: String = UserPreferencesRepository.THEME_SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.minAudioDuration,
        userPreferencesRepository.includedFolders,
        userPreferencesRepository.themeMode
    ) { duration, folders, theme ->
        SettingsUiState(duration, folders, theme)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateMinAudioDuration(durationMs: Long) {
        viewModelScope.launch {
            userPreferencesRepository.setMinAudioDuration(durationMs)
        }
    }

    fun addIncludedFolder(path: String) {
        viewModelScope.launch {
            val current = uiState.value.includedFolders.toMutableSet()
            current.add(path)
            userPreferencesRepository.setIncludedFolders(current)
        }
    }

    fun removeIncludedFolder(path: String) {
        viewModelScope.launch {
            val current = uiState.value.includedFolders.toMutableSet()
            current.remove(path)
            userPreferencesRepository.setIncludedFolders(current)
        }
    }
    
    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            musicRepository.scanLocalMusic()
        }
    }
}
