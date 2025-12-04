package com.sigma.music.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sigma.music.domain.model.Album
import com.sigma.music.domain.model.Artist
import com.sigma.music.domain.model.Song
import com.sigma.music.domain.usecase.GetAlbumsUseCase
import com.sigma.music.domain.usecase.GetArtistsUseCase
import com.sigma.music.domain.usecase.GetRecentlyAddedSongsUseCase
import com.sigma.music.domain.usecase.GetSongsUseCase
import com.sigma.music.domain.usecase.PlaySongUseCase
import com.sigma.music.domain.usecase.ScanLocalMusicUseCase
import com.sigma.music.domain.usecase.ToggleShuffleUseCase
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
    val isLoading: Boolean = false,
    // Selection Mode
    val isSelectionMode: Boolean = false,
    val selectedSongIds: Set<Long> = emptySet(),
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
    private val toggleShuffleUseCase: ToggleShuffleUseCase // Reusing existing UseCase logic for shuffle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isSelectionMode = MutableStateFlow(false)
    private val _selectedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _sortOption = MutableStateFlow(SortOption.TITLE)

    // Group Data Flows
    private val _dataFlow = combine(
        getSongsUseCase(),
        getRecentlyAddedSongsUseCase(),
        getAlbumsUseCase(),
        getArtistsUseCase()
    ) { songs, recent, albums, artists ->
        DataState(songs, recent, albums, artists)
    }

    // Combine Data with UI State
    val uiState: StateFlow<HomeUiState> = combine(
        _dataFlow,
        _isLoading,
        _isSelectionMode,
        _selectedSongIds,
        _sortOption
    ) { data, isLoading, isSelection, selected, sort ->
        // Apply Sorting
        val sortedSongs = when (sort) {
            SortOption.TITLE -> data.songs.sortedBy { it.title }
            SortOption.ARTIST -> data.songs.sortedBy { it.artist }
            SortOption.DATE_ADDED -> data.recent // Or implement proper date sorting
        }

        HomeUiState(
            songs = sortedSongs,
            recentlyAdded = data.recent,
            albums = data.albums,
            artists = data.artists,
            isLoading = isLoading,
            isSelectionMode = isSelection,
            selectedSongIds = selected,
            sortOption = sort
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
        val artists: List<Artist>
    )

    fun scanMusic() {
        viewModelScope.launch {
            _isLoading.value = true
            scanLocalMusicUseCase()
            _isLoading.value = false
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
            // Shuffle logic: Play index 0 but with shuffle mode ON (handled by repo/player usually)
            // Or we can shuffle the list here and play.
            // For now, simpler:
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
                // Optional: Exit selection mode if empty? Or keep it.
                // exitSelectionMode() 
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
    
    fun addToPlaylist() {
        // TODO
        exitSelectionMode()
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
