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

import com.gemini.music.domain.model.ScanStatus
import kotlinx.coroutines.flow.MutableStateFlow

data class SettingsUiState(
    val minAudioDuration: Long = 10000L,
    val includedFolders: Set<String> = emptySet(),
    val themeMode: String = UserPreferencesRepository.THEME_SYSTEM,
    val scanStatus: ScanStatus = ScanStatus.Idle
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.minAudioDuration,
        userPreferencesRepository.includedFolders,
        userPreferencesRepository.themeMode,
        _scanStatus
    ) { duration, folders, theme, scanStatus ->
        SettingsUiState(duration, folders, theme, scanStatus)
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
            musicRepository.scanLocalMusic().collect { status ->
                _scanStatus.value = status
            }
        }
    }
}
