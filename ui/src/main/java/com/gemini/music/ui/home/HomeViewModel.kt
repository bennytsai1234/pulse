package com.gemini.music.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.Album
import com.gemini.music.domain.model.Artist
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.usecase.GetAlbumsUseCase
import com.gemini.music.domain.usecase.GetArtistsUseCase
import com.gemini.music.domain.usecase.GetRecentlyAddedSongsUseCase
import com.gemini.music.domain.usecase.GetSongsUseCase
import com.gemini.music.domain.usecase.PlaySongUseCase
import com.gemini.music.domain.usecase.ScanLocalMusicUseCase
import com.gemini.music.domain.usecase.ToggleShuffleUseCase
import com.gemini.music.domain.model.Playlist
import com.gemini.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption {
    TITLE, ARTIST, DATE_ADDED
}

data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val recentlyAdded: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    // Playlists
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    // Selection Mode
    val isSelectionMode: Boolean = false,
    val selectedSongIds: Set<Long> = emptySet(),
    // Dialogs
    val showAddToPlaylistDialog: Boolean = false,
    // Sorting
    val sortOption: SortOption = SortOption.TITLE
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getSongsUseCase: GetSongsUseCase,
    getRecentlyAddedSongsUseCase: GetRecentlyAddedSongsUseCase,
    getAlbumsUseCase: GetAlbumsUseCase,
    getArtistsUseCase: GetArtistsUseCase,
    private val scanLocalMusicUseCase: ScanLocalMusicUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val toggleShuffleUseCase: ToggleShuffleUseCase,
    private val musicRepository: MusicRepository // Direct Access for Playlist MVP
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isSelectionMode = MutableStateFlow(false)
    private val _selectedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _sortOption = MutableStateFlow(SortOption.TITLE)
    private val _showAddToPlaylistDialog = MutableStateFlow(false)

    // Group Data Flows
    private val _dataFlow = combine(
        getSongsUseCase(),
        getRecentlyAddedSongsUseCase(),
        getAlbumsUseCase(),
        getArtistsUseCase(),
        musicRepository.getPlaylists()
    ) { songs, recent, albums, artists, playlists ->
        DataState(songs, recent, albums, artists, playlists)
    }

    private val _controlsFlow = combine(
        _isLoading,
        _isSelectionMode,
        _selectedSongIds,
        _showAddToPlaylistDialog,
        _sortOption
    ) { isLoading, isSelection, selected, showDialog, sort ->
        ControlsState(isLoading, isSelection, selected, showDialog, sort)
    }

    // Combine Data with UI State
    val uiState: StateFlow<HomeUiState> = combine(
        _dataFlow,
        _controlsFlow
    ) { data, controls ->
        // Apply Sorting
        val sortedSongs = when (controls.sortOption) {
            SortOption.TITLE -> data.songs.sortedBy { it.title }
            SortOption.ARTIST -> data.songs.sortedBy { it.artist }
            SortOption.DATE_ADDED -> data.recent 
        }

        HomeUiState(
            songs = sortedSongs,
            recentlyAdded = data.recent,
            albums = data.albums,
            artists = data.artists,
            playlists = data.playlists,
            isLoading = controls.isLoading,
            isSelectionMode = controls.isSelectionMode,
            selectedSongIds = controls.selectedSongIds,
            showAddToPlaylistDialog = controls.showAddToPlaylistDialog,
            sortOption = controls.sortOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        scanMusic()
    }

    // Helper data class for combine
    data class DataState(
        val songs: List<Song>,
        val recent: List<Song>,
        val albums: List<Album>,
        val artists: List<Artist>,
        val playlists: List<Playlist>
    )

    data class ControlsState(
        val isLoading: Boolean,
        val isSelectionMode: Boolean,
        val selectedSongIds: Set<Long>,
        val showAddToPlaylistDialog: Boolean,
        val sortOption: SortOption
    )

    fun scanMusic() {
        viewModelScope.launch {
            _isLoading.value = true
            // Must collect the Flow to trigger the scan operation
            scanLocalMusicUseCase().collect { status ->
                // Scan completed when we receive Completed or Failed
                when (status) {
                    is com.gemini.music.domain.model.ScanStatus.Completed,
                    is com.gemini.music.domain.model.ScanStatus.Failed -> {
                        _isLoading.value = false
                    }
                    else -> { /* Scanning in progress */ }
                }
            }
        }
    }

    fun playSong(song: Song) {
        if (_isSelectionMode.value) {
            toggleSongSelection(song.id)
        } else {
            val currentList = uiState.value.songs
            val index = currentList.indexOf(song)
            if (index != -1) {
                playSongUseCase(currentList, index)
            }
        }
    }

    fun shuffleAll() {
        val allSongs = uiState.value.songs
        if (allSongs.isNotEmpty()) {
            playSongUseCase(allSongs.shuffled(), 0)
        }
    }

    // --- Selection Mode ---

    fun enterSelectionMode() {
        _isSelectionMode.value = true
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedSongIds.value = emptySet()
    }

    fun toggleSongSelection(songId: Long) {
        val current = _selectedSongIds.value
        if (current.contains(songId)) {
            _selectedSongIds.value = current - songId
            if (_selectedSongIds.value.isEmpty()) {
                // Keep selection mode active even if empty
            }
        } else {
            _selectedSongIds.value = current + songId
            if (!_isSelectionMode.value) {
                _isSelectionMode.value = true
            }
        }
    }

    fun selectAll() {
        val allIds = uiState.value.songs.map { it.id }.toSet()
        if (_selectedSongIds.value.size == allIds.size) {
            _selectedSongIds.value = emptySet() // Deselect all
        } else {
            _selectedSongIds.value = allIds
        }
    }

    fun playSelected() {
        val selectedIds = _selectedSongIds.value
        val selectedSongs = uiState.value.songs.filter { it.id in selectedIds }
        if (selectedSongs.isNotEmpty()) {
            playSongUseCase(selectedSongs, 0)
            exitSelectionMode()
        }
    }
    
    fun addToPlaylistClicked() {
        if (_selectedSongIds.value.isNotEmpty()) {
            _showAddToPlaylistDialog.value = true
        }
    }

    fun dismissAddToPlaylistDialog() {
        _showAddToPlaylistDialog.value = false
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val playlistId = musicRepository.createPlaylist(name)
            // Add selected songs
            _selectedSongIds.value.forEach { songId ->
                musicRepository.addSongToPlaylist(playlistId, songId)
            }
            dismissAddToPlaylistDialog()
            exitSelectionMode()
        }
    }

    fun addSelectedToPlaylist(playlist: Playlist) {
         viewModelScope.launch {
            _selectedSongIds.value.forEach { songId ->
                musicRepository.addSongToPlaylist(playlist.id, songId)
            }
            dismissAddToPlaylistDialog()
            exitSelectionMode()
        }
    }
    
    fun deleteSelected() {
        // TODO: Implement deletion logic with SAF
        exitSelectionMode()
    }

    // --- Sorting ---
    
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }
}
