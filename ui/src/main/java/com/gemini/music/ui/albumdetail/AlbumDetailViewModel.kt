package com.gemini.music.ui.albumdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.Album
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.usecase.GetAlbumByIdUseCase
import com.gemini.music.domain.usecase.GetSongsByAlbumIdUseCase
import com.gemini.music.domain.usecase.PlayAlbumUseCase
import com.gemini.music.domain.usecase.PlaySongUseCase
import com.gemini.music.domain.usecase.ShuffleAndPlayAlbumUseCase
import com.gemini.music.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumDetailUiState(
    val album: Album? = null,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val getAlbumByIdUseCase: GetAlbumByIdUseCase,
    private val getSongsByAlbumIdUseCase: GetSongsByAlbumIdUseCase,
    private val playSongUseCase: PlaySongUseCase,
    private val playAlbumUseCase: PlayAlbumUseCase,
    private val shuffleAndPlayAlbumUseCase: ShuffleAndPlayAlbumUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: Long = savedStateHandle[Screen.AlbumDetail.albumIdArg] ?: -1L

    val uiState: StateFlow<AlbumDetailUiState> =
        if (albumId == -1L) {
            MutableStateFlow(AlbumDetailUiState(isLoading = false))
        } else {
            combine(
                getAlbumByIdUseCase(albumId),
                getSongsByAlbumIdUseCase(albumId)
            ) { album, songs ->
                AlbumDetailUiState(album, songs, isLoading = false)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AlbumDetailUiState(isLoading = true)
            )
        }

    fun playSong(song: Song) {
        val currentList = uiState.value.songs
        val index = currentList.indexOf(song)
        if (index != -1) {
            playSongUseCase(currentList, index)
        }
    }

    fun playAllSongs() {
        if (albumId != -1L) {
            viewModelScope.launch {
                playAlbumUseCase(albumId)
            }
        }
    }

    fun shuffleAllSongs() {
        if (albumId != -1L) {
            viewModelScope.launch {
                shuffleAndPlayAlbumUseCase(albumId)
            }
        }
    }
}
