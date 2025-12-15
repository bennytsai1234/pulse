package com.gemini.music.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.music.domain.model.Recommendation
import com.gemini.music.domain.model.RecommendationPlaylist
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicController
import com.gemini.music.domain.usecase.recommendation.GetDailyMixUseCase
import com.gemini.music.domain.usecase.recommendation.GetRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = true,
    val dailyMix: RecommendationPlaylist? = null,
    val recommendations: List<Recommendation> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getDailyMixUseCase: GetDailyMixUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val musicController: MusicController
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()
    
    init {
        loadRecommendations()
    }
    
    private fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Load Daily Mix
            launch {
                getDailyMixUseCase()
                    .catch { e -> 
                        _uiState.update { it.copy(error = e.message) }
                    }
                    .collect { dailyMix ->
                        _uiState.update { it.copy(dailyMix = dailyMix, isLoading = false) }
                    }
            }
            
            // Load Recommendations
            launch {
                getRecommendationsUseCase(limit = 20)
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
                    .collect { recommendations ->
                        _uiState.update { it.copy(recommendations = recommendations, isLoading = false) }
                    }
            }
        }
    }
    
    fun playDailyMix() {
        val songs = _uiState.value.dailyMix?.songs ?: return
        if (songs.isNotEmpty()) {
            musicController.playSongs(songs, startIndex = 0)
        }
    }
    
    fun shuffleDailyMix() {
        val songs = _uiState.value.dailyMix?.songs?.shuffled() ?: return
        if (songs.isNotEmpty()) {
            musicController.playSongs(songs, startIndex = 0)
        }
    }
    
    fun playSong(song: Song) {
        val songs = _uiState.value.recommendations.map { it.song }
        val index = songs.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            musicController.playSongs(songs, startIndex = index)
        }
    }
    
    fun playRecommendation(recommendation: Recommendation) {
        playSong(recommendation.song)
    }
    
    fun refresh() {
        loadRecommendations()
    }
}
