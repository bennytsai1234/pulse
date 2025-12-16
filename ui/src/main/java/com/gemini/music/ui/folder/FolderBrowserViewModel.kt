package com.gemini.music.ui.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.FolderContent
import com.gemini.music.domain.model.MusicFolder
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.FolderRepository
import com.gemini.music.domain.repository.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FolderBrowserUiState(
    val isLoading: Boolean = true,
    val rootFolders: List<MusicFolder> = emptyList(),
    val currentPath: String? = null,
    val currentContent: FolderContent? = null,
    val breadcrumbs: List<Pair<String, String>> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Song> = emptyList(),
    val isSearching: Boolean = false,
    val viewMode: FolderViewMode = FolderViewMode.GRID,
    val sortBy: FolderSortBy = FolderSortBy.NAME,
    val sortAscending: Boolean = true,
    val errorMessage: String? = null
)

enum class FolderViewMode { GRID, LIST }

enum class FolderSortBy { NAME, DATE, SONG_COUNT }

@HiltViewModel
class FolderBrowserViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val musicController: MusicController
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FolderBrowserUiState())
    val uiState: StateFlow<FolderBrowserUiState> = _uiState.asStateFlow()
    
    init {
        loadRootFolders()
    }
    
    private fun loadRootFolders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            folderRepository.getRootFolders().collect { folders ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rootFolders = folders.sortedWith(getCurrentComparator()),
                        currentPath = null,
                        currentContent = null,
                        breadcrumbs = emptyList(),
                        errorMessage = null
                    )
                }
            }
        }
    }
    
    fun navigateToFolder(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            folderRepository.getFolderContent(path).collect { content ->
                val breadcrumbs = folderRepository.getBreadcrumbs(path)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPath = path,
                        currentContent = content,
                        breadcrumbs = breadcrumbs,
                        searchQuery = "",
                        searchResults = emptyList(),
                        isSearching = false
                    )
                }
            }
        }
    }
    
    fun navigateUp(): Boolean {
        val currentPath = _uiState.value.currentPath ?: return false
        val parentPath = folderRepository.getParentPath(currentPath)
        
        if (parentPath != null) {
            navigateToFolder(parentPath)
            return true
        } else {
            loadRootFolders()
            return true
        }
    }
    
    fun navigateToRoot() {
        loadRootFolders()
    }
    
    fun navigateToBreadcrumb(path: String) {
        navigateToFolder(path)
    }
    
    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        
        val currentPath = _uiState.value.currentPath ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            folderRepository.searchInFolder(currentPath, query).collect { results ->
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isSearching = false
                    )
                }
            }
        }
    }
    
    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false) }
    }
    
    fun setViewMode(mode: FolderViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }
    
    fun setSortBy(sortBy: FolderSortBy) {
        val currentSort = _uiState.value.sortBy
        val newAscending = if (currentSort == sortBy) !_uiState.value.sortAscending else true
        
        _uiState.update {
            it.copy(
                sortBy = sortBy,
                sortAscending = newAscending,
                rootFolders = it.rootFolders.sortedWith(getComparator(sortBy, newAscending))
            )
        }
    }
    
    private fun getCurrentComparator(): Comparator<MusicFolder> {
        return getComparator(_uiState.value.sortBy, _uiState.value.sortAscending)
    }
    
    private fun getComparator(sortBy: FolderSortBy, ascending: Boolean): Comparator<MusicFolder> {
        val comparator = when (sortBy) {
            FolderSortBy.NAME -> compareBy<MusicFolder> { it.name.lowercase() }
            FolderSortBy.DATE -> compareBy { it.lastModified }
            FolderSortBy.SONG_COUNT -> compareBy { it.songCount }
        }
        return if (ascending) comparator else comparator.reversed()
    }
    
    /**
     * 播放歌曲
     */
    fun playSong(song: Song, allSongsInFolder: List<Song> = emptyList()) {
        val songs = if (allSongsInFolder.isNotEmpty()) {
            allSongsInFolder
        } else {
            _uiState.value.currentContent?.songs ?: listOf(song)
        }
        val startIndex = songs.indexOf(song).coerceAtLeast(0)
        musicController.playSongs(songs, startIndex)
    }
    
    // ==================== Utility ====================
    
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
