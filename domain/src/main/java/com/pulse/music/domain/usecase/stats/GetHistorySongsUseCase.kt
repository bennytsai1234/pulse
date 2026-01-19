package com.pulse.music.domain.usecase.stats

import com.pulse.music.domain.model.PlaybackRecord
import com.pulse.music.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistorySongsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    operator fun invoke(limit: Int = 100): Flow<List<PlaybackRecord>> {
        return statsRepository.getRecentlyPlayed(limit)
    }
}
