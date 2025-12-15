package com.gemini.music.domain.usecase.recommendation

import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 取得藝人電台的 UseCase。
 * 根據指定藝人生成混合播放清單。
 */
class GetArtistRadioUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(artistName: String): Flow<List<Song>> {
        return recommendationRepository.getArtistRadio(artistName)
    }
}
