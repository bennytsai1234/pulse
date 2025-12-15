package com.gemini.music.ui.tageditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.SongTags
import com.gemini.music.domain.usecase.tageditor.GetSongTagsUseCase
import com.gemini.music.domain.usecase.tageditor.UpdateSongTagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagEditorUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,
    val originalTags: SongTags? = null,
    // Editable Fields
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArtist: String = "",
    val genre: String = "",
    val year: String = "",
    val trackNumber: String = "",
    val discNumber: String = "",
    val comment: String = ""
) {
    val hasChanges: Boolean
        get() = originalTags?.let {
            title != it.title ||
            artist != it.artist ||
            album != it.album ||
            albumArtist != it.albumArtist ||
            genre != it.genre ||
            year != it.year ||
            trackNumber != it.trackNumber ||
            discNumber != it.discNumber ||
            comment != it.comment
        } ?: false
}

sealed interface TagEditorEvent {
    data class UpdateTitle(val value: String) : TagEditorEvent
    data class UpdateArtist(val value: String) : TagEditorEvent
    data class UpdateAlbum(val value: String) : TagEditorEvent
    data class UpdateAlbumArtist(val value: String) : TagEditorEvent
    data class UpdateGenre(val value: String) : TagEditorEvent
    data class UpdateYear(val value: String) : TagEditorEvent
    data class UpdateTrackNumber(val value: String) : TagEditorEvent
    data class UpdateDiscNumber(val value: String) : TagEditorEvent
    data class UpdateComment(val value: String) : TagEditorEvent
    data object Save : TagEditorEvent
    data object DismissError : TagEditorEvent
}

@HiltViewModel
class TagEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSongTagsUseCase: GetSongTagsUseCase,
    private val updateSongTagsUseCase: UpdateSongTagsUseCase
) : ViewModel() {

    private val songId: Long = savedStateHandle["songId"] ?: 0L

    private val _uiState = MutableStateFlow(TagEditorUiState())
    val uiState: StateFlow<TagEditorUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tags = getSongTagsUseCase(songId)
            if (tags != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        originalTags = tags,
                        title = tags.title,
                        artist = tags.artist,
                        album = tags.album,
                        albumArtist = tags.albumArtist,
                        genre = tags.genre,
                        year = tags.year,
                        trackNumber = tags.trackNumber,
                        discNumber = tags.discNumber,
                        comment = tags.comment
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveError = "Failed to load tags"
                    )
                }
            }
        }
    }

    fun onEvent(event: TagEditorEvent) {
        when (event) {
            is TagEditorEvent.UpdateTitle -> _uiState.update { it.copy(title = event.value) }
            is TagEditorEvent.UpdateArtist -> _uiState.update { it.copy(artist = event.value) }
            is TagEditorEvent.UpdateAlbum -> _uiState.update { it.copy(album = event.value) }
            is TagEditorEvent.UpdateAlbumArtist -> _uiState.update { it.copy(albumArtist = event.value) }
            is TagEditorEvent.UpdateGenre -> _uiState.update { it.copy(genre = event.value) }
            is TagEditorEvent.UpdateYear -> _uiState.update { it.copy(year = event.value) }
            is TagEditorEvent.UpdateTrackNumber -> _uiState.update { it.copy(trackNumber = event.value) }
            is TagEditorEvent.UpdateDiscNumber -> _uiState.update { it.copy(discNumber = event.value) }
            is TagEditorEvent.UpdateComment -> _uiState.update { it.copy(comment = event.value) }
            is TagEditorEvent.DismissError -> _uiState.update { it.copy(saveError = null) }
            is TagEditorEvent.Save -> saveTags()
        }
    }

    private fun saveTags() {
        val originalTags = _uiState.value.originalTags ?: return
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            
            val updatedTags = originalTags.copy(
                title = state.title,
                artist = state.artist,
                album = state.album,
                albumArtist = state.albumArtist,
                genre = state.genre,
                year = state.year,
                trackNumber = state.trackNumber,
                discNumber = state.discNumber,
                comment = state.comment
            )
            
            val success = updateSongTagsUseCase(updatedTags)
            
            if (success) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        originalTags = updatedTags
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = "Failed to save tags. The file may be read-only."
                    )
                }
            }
        }
    }
}
