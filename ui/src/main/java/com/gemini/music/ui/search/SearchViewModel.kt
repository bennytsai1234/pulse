package com.gemini.music.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.usecase.GetSongsUseCase
import com.gemini.music.domain.usecase.PlaySongUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.gemini.music.domain.model.Album
import com.gemini.music.domain.model.Artist
import com.gemini.music.domain.repository.SearchRepository
import com.gemini.music.domain.usecase.GetAlbumsUseCase
import com.gemini.music.domain.usecase.GetArtistsUseCase

data class SearchUiState(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val recentSearches: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    getSongsUseCase: GetSongsUseCase,
    getAlbumsUseCase: GetAlbumsUseCase,
    getArtistsUseCase: GetArtistsUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val allSongs = getSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val allAlbums = getAlbumsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val allArtists = getArtistsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val recentSearches = searchRepository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _searchQuery
        .debounce(300L)

    val uiState: StateFlow<SearchUiState> = combine(
        debouncedQuery,
        allSongs,
        allAlbums,
        allArtists,
        recentSearches
    ) { query, songs, albums, artists, recent ->
        if (query.isBlank()) {
            SearchUiState(query = query, recentSearches = recent)
        } else {
            SearchUiState(
                query = query,
                songs = songs.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.artist.contains(query, ignoreCase = true) 
                },
                albums = albums.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.artist.contains(query, ignoreCase = true) 
                },
                artists = artists.filter { 
                    it.name.contains(query, ignoreCase = true) 
                },
                recentSearches = recent
            )
        }
    }
    .flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSongClick(song: Song) {
        addToHistory(_searchQuery.value)
        val currentResults = uiState.value.songs
        val index = currentResults.indexOf(song)
        if (index != -1) {
            playSongUseCase(currentResults, index)
        }
    }
    
    fun onAlbumClick(@Suppress("UNUSED_PARAMETER") album: Album) {
        addToHistory(_searchQuery.value)
    }

    fun onArtistClick(@Suppress("UNUSED_PARAMETER") artist: Artist) {
        addToHistory(_searchQuery.value)
    }
    
    fun addToHistory(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                searchRepository.addSearch(query)
            }
        }
    }

    fun removeHistoryItem(query: String) {
        viewModelScope.launch {
            searchRepository.removeSearch(query)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchRepository.clearHistory()
        }
    }
}
