package com.gemini.music.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.ArtistPlayStats
import com.gemini.music.domain.model.ListeningStatsOverview
import com.gemini.music.domain.model.PlaybackRecord
import com.gemini.music.domain.model.SmartPlaylist
import com.gemini.music.domain.model.SmartPlaylistType
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.model.SongPlayStats
import com.gemini.music.domain.repository.ListeningStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val overview: ListeningStatsOverview = ListeningStatsOverview(),
    val topSongs: List<SongPlayStats> = emptyList(),
    val topArtists: List<ArtistPlayStats> = emptyList(),
    val recentlyPlayed: List<PlaybackRecord> = emptyList(),
    val smartPlaylists: List<SmartPlaylist> = emptyList(),
    val selectedSmartPlaylistType: SmartPlaylistType? = null,
    val smartPlaylistSongs: List<Song> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val listeningStatsRepository: ListeningStatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    
    init {
        loadStats()
        loadSmartPlaylists()
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val overview = listeningStatsRepository.getStatsOverview()
                val topSongs = listeningStatsRepository.getMostPlayedSongs(20).first()
                val topArtists = listeningStatsRepository.getMostPlayedArtists(10).first()
                val recentlyPlayed = listeningStatsRepository.getRecentlyPlayed(20).first()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        overview = overview,
                        topSongs = topSongs,
                        topArtists = topArtists,
                        recentlyPlayed = recentlyPlayed,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load stats: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun loadSmartPlaylists() {
        viewModelScope.launch {
            listeningStatsRepository.getSmartPlaylists().collect { playlists ->
                _uiState.update { it.copy(smartPlaylists = playlists) }
            }
        }
    }
    
    fun selectSmartPlaylist(type: SmartPlaylistType) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSmartPlaylistType = type, smartPlaylistSongs = emptyList()) }
            
            listeningStatsRepository.getSongsForSmartPlaylist(type, 100).collect { songs ->
                _uiState.update { it.copy(smartPlaylistSongs = songs) }
            }
        }
    }
    
    fun clearSmartPlaylistSelection() {
        _uiState.update { it.copy(selectedSmartPlaylistType = null, smartPlaylistSongs = emptyList()) }
    }
    
    fun refreshStats() {
        loadStats()
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            listeningStatsRepository.clearAllHistory()
            loadStats()
        }
    }
    
    // ==================== Utility ====================
    
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }
    
    fun formatTotalTime(millis: Long): String {
        val totalMinutes = millis / 60000
        val hours = totalMinutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${totalMinutes % 60}m"
            else -> "${totalMinutes}m"
        }
    }
}
