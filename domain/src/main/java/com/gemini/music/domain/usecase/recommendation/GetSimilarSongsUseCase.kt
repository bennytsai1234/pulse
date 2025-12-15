package com.gemini.music.domain.usecase.recommendation

import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 取得類似歌曲推薦的 UseCase。
 */
class GetSimilarSongsUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(songId: Long, limit: Int = 10): Flow<List<Song>> {
        return recommendationRepository.getSimilarSongs(songId, limit)
    }
}
