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
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    getSongsUseCase: GetSongsUseCase,
    private val playSongUseCase: PlaySongUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val allSongs = getSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val searchResults: StateFlow<List<Song>> = combine(
        _searchQuery,
        allSongs
    ) { query, songs ->
        if (query.isBlank()) {
            emptyList()
        } else {
            songs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSongClick(song: Song) {
        // When searching, usually we play that specific song, 
        // or create a queue from the search results.
        // For simplicity, we play the song in context of search results
        val currentResults = searchResults.value
        val index = currentResults.indexOf(song)
        if (index != -1) {
            playSongUseCase(currentResults, index)
        }
    }
}
