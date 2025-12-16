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
import com.gemini.music.domain.repository.MusicController
import com.gemini.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption {
    TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION
}

@androidx.compose.runtime.Immutable
data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val recentlyAdded: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    // Playlists
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val filterFavorites: Boolean = false,
    // Selection Mode
    val isSelectionMode: Boolean = false,
    val selectedSongIds: Set<Long> = emptySet(),
    // Dialogs
    val showAddToPlaylistDialog: Boolean = false,
    // Sorting
    val sortOption: SortOption = SortOption.TITLE,
    // 播放狀態
    val currentPlayingSongId: Long? = null,
    val isPlaying: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getSongsUseCase: GetSongsUseCase,
    getRecentlyAddedSongsUseCase: GetRecentlyAddedSongsUseCase,
    getAlbumsUseCase: GetAlbumsUseCase,
    getArtistsUseCase: GetArtistsUseCase,
    private val scanLocalMusicUseCase: ScanLocalMusicUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val deleteSongUseCase: com.gemini.music.domain.usecase.DeleteSongUseCase,
    private val getFavoriteSongsUseCase: com.gemini.music.domain.usecase.favorites.GetFavoriteSongsUseCase,
    private val musicRepository: MusicRepository, // Direct Access for Playlist MVP
    private val musicController: MusicController,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    
    // Process Death Resilient States
    private val _isSelectionMode = savedStateHandle.getStateFlow("is_selection_mode", false)
    private val _selectedSongIds = savedStateHandle.getStateFlow<Set<Long>>("selected_song_ids", emptySet())
    private val _sortOption = savedStateHandle.getStateFlow("sort_option", SortOption.TITLE)
    private val _filterFavorites = savedStateHandle.getStateFlow("filter_favorites", false)
    private val _showAddToPlaylistDialog = MutableStateFlow(false)
    
    // Error Handling for Deletion (Android 10+)
    private val _recoverableAction = MutableStateFlow<android.app.RecoverableSecurityException?>(null)
    val recoverableAction: StateFlow<android.app.RecoverableSecurityException?> = _recoverableAction.asStateFlow()

    // Group Data Flows - Using nested combine since Kotlin combine supports max 5 flows
    private val _sourcesFlow = combine(
        getSongsUseCase(),
        getRecentlyAddedSongsUseCase(),
        getAlbumsUseCase()
    ) { songs, recent, albums -> Triple(songs, recent, albums) }
    
    private val _extraDataFlow = combine(
        getArtistsUseCase(),
        musicRepository.getPlaylists(),
        getFavoriteSongsUseCase()
    ) { artists, playlists, favorites -> Triple(artists, playlists, favorites) }
    
    private val _dataFlow = combine(_sourcesFlow, _extraDataFlow) { sources, extra ->
        DataState(
            songs = sources.first,
            recent = sources.second,
            albums = sources.third,
            artists = extra.first,
            playlists = extra.second,
            favorites = extra.third
        )
    }

    private val _controlsFlow = combine(
        _isLoading,
        _isSelectionMode,
        _selectedSongIds,
        _showAddToPlaylistDialog,
        combine(_sortOption, _filterFavorites) { sort, filter -> sort to filter }
    ) { isLoading, isSelection, selected, showDialog, sortAndFilter ->
        ControlsState(isLoading, isSelection, selected, showDialog, sortAndFilter.first, sortAndFilter.second)
    }

    // Combine Data with UI State
    val uiState: StateFlow<HomeUiState> = combine(
        _dataFlow,
        _controlsFlow,
        musicController.musicState
    ) { data, controls, playState ->
        // Select source list based on filter
        val sourceList = if (controls.filterFavorites) data.favorites else data.songs

        // Apply Sorting
        val sortedSongs = when (controls.sortOption) {
            SortOption.TITLE -> sourceList.sortedBy { it.title }
            SortOption.ARTIST -> sourceList.sortedBy { it.artist }
            SortOption.ALBUM -> sourceList.sortedBy { it.album }
            SortOption.DATE_ADDED -> sourceList.sortedByDescending { it.dateAdded }
            SortOption.DURATION -> sourceList.sortedByDescending { it.duration }
        }

        HomeUiState(
            songs = sortedSongs,
            recentlyAdded = data.recent,
            albums = data.albums,
            artists = data.artists,
            playlists = data.playlists,
            isLoading = controls.isLoading,
            filterFavorites = controls.filterFavorites,
            isSelectionMode = controls.isSelectionMode,
            selectedSongIds = controls.selectedSongIds,
            showAddToPlaylistDialog = controls.showAddToPlaylistDialog,
            sortOption = controls.sortOption,
            currentPlayingSongId = playState.currentSong?.id,
            isPlaying = playState.isPlaying
        )
    }
    .flowOn(Dispatchers.Default) // Perform sorting and combination on Background Thread
    .stateIn(
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
        val playlists: List<Playlist>,
        val favorites: List<Song>
    )

    data class ControlsState(
        val isLoading: Boolean,
        val isSelectionMode: Boolean,
        val selectedSongIds: Set<Long>,
        val showAddToPlaylistDialog: Boolean,
        val sortOption: SortOption,
        val filterFavorites: Boolean
    )

    fun scanMusic() {
        if (_isLoading.value) return // Prevent overlapping scans
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
        savedStateHandle["is_selection_mode"] = true
    }

    fun exitSelectionMode() {
        savedStateHandle["is_selection_mode"] = false
        savedStateHandle["selected_song_ids"] = emptySet<Long>()
    }

    fun toggleSongSelection(songId: Long) {
        val current = _selectedSongIds.value
        if (current.contains(songId)) {
            savedStateHandle["selected_song_ids"] = current - songId
            if (_selectedSongIds.value.isEmpty()) {
                // Keep selection mode active even if empty
            }
        } else {
            savedStateHandle["selected_song_ids"] = current + songId
            if (!_isSelectionMode.value) {
                savedStateHandle["is_selection_mode"] = true
            }
        }
    }

    fun selectAll() {
        val allIds = uiState.value.songs.map { it.id }.toSet()
        if (_selectedSongIds.value.size == allIds.size) {
            savedStateHandle["selected_song_ids"] = emptySet<Long>() // Deselect all
        } else {
            savedStateHandle["selected_song_ids"] = allIds
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
        viewModelScope.launch {
             val selectedIds = _selectedSongIds.value
             val songsToDelete = uiState.value.songs.filter { it.id in selectedIds }
             
             // Process sequentially to handle permissions one by one (though ideally batching is better but harder with RecoverableSecurityException loop)
             // For simplicity, we try to delete all. If one fails with RecoverableSecurityException, we stop and ask user.
             // User will have to retry deletion.
             
             songsToDelete.forEach { song ->
                 try {
                     deleteSongUseCase(song)
                     // If success, remove from selection so we don't try to delete again on retry
                     toggleSongSelection(song.id)
                 } catch (e: SecurityException) {
                     // RecoverableSecurityException is only available on API 29+
                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q 
                         && e is android.app.RecoverableSecurityException) {
                         // Pause and ask for permission
                         _recoverableAction.value = e
                         return@launch // Stop processing to handle this one
                     } else {
                         e.printStackTrace()
                     }
                 } catch (e: Exception) {
                     e.printStackTrace()
                     // Ignore other errors or show toast?
                 }
             }
             
             if (_selectedSongIds.value.isEmpty()) {
                 exitSelectionMode()
             }
        }
    }
    
    fun handleRecoverableAction(@Suppress("UNUSED_PARAMETER") resultCode: Int) {
        // If result is OK, user granted permission.
        // We could retry the deletion here.
        // For now, we clear the exception so UI can reset.
        _recoverableAction.value = null
        // Ideally we would retry the last failed operation. 
        // Simplest UX: User clicks delete again.
    }

    // --- Sorting ---
    
    fun setSortOption(option: SortOption) {
        savedStateHandle["sort_option"] = option
    }
    
    fun toggleFavoritesFilter() {
        savedStateHandle["filter_favorites"] = !(_filterFavorites.value)
    }
    
    /**
     * 切換單曲的最愛狀態
     */
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId)
        }
    }
}
