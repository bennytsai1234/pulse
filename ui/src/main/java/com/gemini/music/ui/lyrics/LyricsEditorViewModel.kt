package com.gemini.music.ui.lyrics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.EditableLyricLine
import com.gemini.music.domain.model.LyricsEdit
import com.gemini.music.domain.model.LyricsSyncEvent
import com.gemini.music.domain.repository.LyricsEditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LyricsEditorUiState(
    val songId: Long = 0,
    val lyrics: LyricsEdit? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedLineIndex: Int? = null,
    val globalOffset: Long = 0,
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LyricsEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lyricsEditRepository: LyricsEditRepository
) : ViewModel() {
    
    private val songId: Long = savedStateHandle.get<Long>("songId") ?: 0L
    
    private val _uiState = MutableStateFlow(LyricsEditorUiState(songId = songId))
    val uiState: StateFlow<LyricsEditorUiState> = _uiState.asStateFlow()
    
    init {
        loadLyrics()
    }
    
    private fun loadLyrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            lyricsEditRepository.getEditableLyrics(songId).collect { lyrics ->
                _uiState.update { 
                    it.copy(
                        lyrics = lyrics,
                        globalOffset = lyrics?.offset ?: 0,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun selectLine(index: Int?) {
        _uiState.update { it.copy(selectedLineIndex = index) }
    }
    
    fun adjustLineTimestamp(lineIndex: Int, newTimestamp: Long) {
        viewModelScope.launch {
            lyricsEditRepository.applySyncEvent(
                songId,
                LyricsSyncEvent.AdjustLineTimestamp(lineIndex, newTimestamp)
            )
            _uiState.update { it.copy(hasUnsavedChanges = true) }
        }
    }
    
    fun adjustGlobalOffset(offset: Long) {
        viewModelScope.launch {
            lyricsEditRepository.adjustGlobalOffset(songId, offset)
            _uiState.update { 
                it.copy(
                    globalOffset = offset,
                    hasUnsavedChanges = true
                )
            }
        }
    }
    
    fun updateLineText(lineIndex: Int, newText: String) {
        viewModelScope.launch {
            lyricsEditRepository.applySyncEvent(
                songId,
                LyricsSyncEvent.UpdateLineText(lineIndex, newText)
            )
            _uiState.update { it.copy(hasUnsavedChanges = true) }
        }
    }
    
    fun insertLine(afterIndex: Int, timestamp: Long, text: String) {
        viewModelScope.launch {
            val newLine = EditableLyricLine(
                index = afterIndex + 1,
                timestamp = timestamp,
                text = text
            )
            lyricsEditRepository.applySyncEvent(
                songId,
                LyricsSyncEvent.InsertLine(afterIndex, newLine)
            )
            _uiState.update { it.copy(hasUnsavedChanges = true) }
        }
    }
    
    fun deleteLine(index: Int) {
        viewModelScope.launch {
            lyricsEditRepository.applySyncEvent(
                songId,
                LyricsSyncEvent.DeleteLine(index)
            )
            _uiState.update { 
                it.copy(
                    hasUnsavedChanges = true,
                    selectedLineIndex = null
                )
            }
        }
    }
    
    fun importFromLrc(lrcContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val success = lyricsEditRepository.importFromLrc(songId, lrcContent)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    successMessage = if (success) "歌詞匯入成功" else null,
                    error = if (!success) "歌詞匯入失敗" else null
                )
            }
        }
    }
    
    fun exportToLrc(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val lrc = lyricsEditRepository.exportToLrc(songId)
            onResult(lrc)
        }
    }
    
    fun saveLyrics() {
        val lyrics = _uiState.value.lyrics ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            lyricsEditRepository.saveLyrics(lyrics)
            _uiState.update { 
                it.copy(
                    isSaving = false,
                    hasUnsavedChanges = false,
                    successMessage = "歌詞已儲存"
                )
            }
        }
    }
    
    /**
     * 將歌詞嵌入到音訊檔案中
     */
    fun embedToFile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val success = lyricsEditRepository.embedLyricsToFile(songId)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    successMessage = if (success) "歌詞已嵌入到音訊檔案" else null,
                    error = if (!success) "嵌入歌詞失敗，請確認檔案可寫入" else null
                )
            }
        }
    }
    
    /**
     * 從音訊檔案中提取嵌入的歌詞
     */
    fun extractFromFile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val extracted = lyricsEditRepository.extractEmbeddedLyrics(songId)
            if (extracted != null) {
                lyricsEditRepository.saveLyrics(extracted)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "已從音訊檔案中提取歌詞"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "音訊檔案中沒有嵌入的歌詞"
                    )
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
