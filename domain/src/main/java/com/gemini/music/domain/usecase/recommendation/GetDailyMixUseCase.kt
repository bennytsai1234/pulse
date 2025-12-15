package com.gemini.music.domain.usecase.recommendation

import com.gemini.music.domain.model.RecommendationPlaylist
import com.gemini.music.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 取得每日精選播放清單的 UseCase。
 * 基於用戶聆聽習慣生成個人化的每日推薦。
 */
class GetDailyMixUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(): Flow<RecommendationPlaylist> {
        return recommendationRepository.getDailyMix()
    }
}
