package com.gemini.music.ui.playlist.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
import com.gemini.music.domain.usecase.PlaySongUseCase
import com.gemini.music.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: com.gemini.music.domain.model.Playlist? = null,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playSongUseCase: PlaySongUseCase
) : ViewModel() {

    private val playlistId: Long = checkNotNull(savedStateHandle[Screen.PlaylistDetail.playlistIdArg])

    val uiState: StateFlow<PlaylistDetailUiState> = kotlinx.coroutines.flow.combine(
        musicRepository.getPlaylist(playlistId),
        musicRepository.getSongsForPlaylist(playlistId)
    ) { playlist, songs ->
        PlaylistDetailUiState(
            playlist = playlist,
            songs = songs,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistDetailUiState()
    )

    fun playSong(song: Song) {
        val songs = uiState.value.songs
        val index = songs.indexOf(song)
        if (index != -1) {
            playSongUseCase(songs, index)
        }
    }

    fun removeSong(songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }
}
