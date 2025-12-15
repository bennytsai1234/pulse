package com.gemini.music.domain.usecase.recommendation

import com.gemini.music.domain.model.Recommendation
import com.gemini.music.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 取得個人化推薦歌曲的 UseCase。
 */
class GetRecommendationsUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<Recommendation>> {
        return recommendationRepository.getRecommendations(limit)
    }
}
