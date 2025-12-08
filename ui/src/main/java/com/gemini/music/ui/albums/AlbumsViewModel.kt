package com.gemini.music.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.Album
import com.gemini.music.domain.usecase.GetAlbumsUseCase
import com.gemini.music.domain.usecase.GetArtistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AlbumsUiState(
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    getAlbumsUseCase: GetAlbumsUseCase
) : ViewModel() {

    val uiState: StateFlow<AlbumsUiState> = getAlbumsUseCase()
        .combine(MutableStateFlow(false)) { albums, isLoading ->
            AlbumsUiState(albums = albums, isLoading = isLoading)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlbumsUiState(isLoading = true)
        )
}
