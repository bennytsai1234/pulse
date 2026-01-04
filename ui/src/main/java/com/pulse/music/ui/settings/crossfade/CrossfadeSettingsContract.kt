package com.pulse.music.ui.settings.crossfade

import com.pulse.music.domain.model.CrossfadeCurve
import com.pulse.music.domain.model.CrossfadeSettings

/**
 * 交叉淡入淡出設定 UI 狀態
 */
data class CrossfadeSettingsUiState(
    val enabled: Boolean = false,
    val durationSeconds: Int = 5,
    val curve: CrossfadeCurve = CrossfadeCurve.LINEAR,
    val applyOnManualSkip: Boolean = true,
    val albumContinuous: Boolean = true,
    val silenceDetection: Boolean = false,
    val isLoading: Boolean = true,
    val showAdvancedSettings: Boolean = false
) {
    companion object {
        fun fromSettings(settings: CrossfadeSettings) = CrossfadeSettingsUiState(
            enabled = settings.enabled,
            durationSeconds = settings.durationSeconds,
            curve = settings.curve,
            applyOnManualSkip = settings.applyOnManualSkip,
            albumContinuous = settings.albumContinuous,
            silenceDetection = settings.silenceDetection,
            isLoading = false
        )

        /** 可選的時長選項 (秒) */
        val DURATION_OPTIONS = listOf(1, 2, 3, 4, 5, 6, 8, 10, 12)
    }
}

/**
 * 交叉淡入淡出設定 UI 事件
 */
sealed class CrossfadeSettingsUiEvent {
    data class SetEnabled(val enabled: Boolean) : CrossfadeSettingsUiEvent()
    data class SetDuration(val seconds: Int) : CrossfadeSettingsUiEvent()
    data class SetCurve(val curve: CrossfadeCurve) : CrossfadeSettingsUiEvent()
    data class SetApplyOnManualSkip(val apply: Boolean) : CrossfadeSettingsUiEvent()
    data class SetAlbumContinuous(val enabled: Boolean) : CrossfadeSettingsUiEvent()
    data class SetSilenceDetection(val enabled: Boolean) : CrossfadeSettingsUiEvent()
    object ToggleAdvancedSettings : CrossfadeSettingsUiEvent()
}
