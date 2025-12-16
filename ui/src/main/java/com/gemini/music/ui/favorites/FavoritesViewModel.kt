package com.gemini.music.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
import com.gemini.music.domain.usecase.PlaySongUseCase
import com.gemini.music.domain.usecase.favorites.GetFavoriteSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = getFavoriteSongsUseCase()
        .map { songs ->
            FavoritesUiState(
                songs = songs,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesUiState(isLoading = true)
        )

    fun onSongClick(index: Int) {
        // Play the list starting from clicked index
        playSongUseCase(uiState.value.songs, index)
    }
    
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId)
        }
    }
}

data class FavoritesUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false
)

