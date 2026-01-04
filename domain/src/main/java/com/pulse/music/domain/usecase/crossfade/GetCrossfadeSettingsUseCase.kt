package com.pulse.music.domain.usecase.crossfade

import com.pulse.music.domain.model.CrossfadeSettings
import com.pulse.music.domain.repository.CrossfadeSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 取得交叉淡入淡出設定的 Use Case
 */
class GetCrossfadeSettingsUseCase @Inject constructor(
    private val repository: CrossfadeSettingsRepository
) {
    /**
     * 取得完整的交叉淡入淡出設定 Flow
     */
    operator fun invoke(): Flow<CrossfadeSettings> = repository.crossfadeSettings

    /**
     * 取得是否啟用交叉淡入淡出
     */
    fun isEnabled(): Flow<Boolean> = repository.enabled

    /**
     * 取得淡入淡出時長 (毫秒)
     */
    fun getDurationMs(): Flow<Int> = repository.durationMs
}
