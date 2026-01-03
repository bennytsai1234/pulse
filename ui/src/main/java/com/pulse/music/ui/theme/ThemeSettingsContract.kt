package com.pulse.music.ui.theme

import com.pulse.music.core.common.base.UiEffect
import com.pulse.music.core.common.base.UiEvent
import com.pulse.music.core.common.base.UiState
import com.pulse.music.domain.model.CustomTheme
import com.pulse.music.domain.model.ThemeMode
import com.pulse.music.domain.model.ThemePalette

/**
 * 主題設定 UI 狀態
 */
data class ThemeSettingsUiState(
    val currentMode: ThemeMode = ThemeMode.SYSTEM,
    val currentPalette: ThemePalette = ThemePalette.PULSE,
    val useDynamicColor: Boolean = false,
    val useAmoledBlack: Boolean = false,
    val contrastLevel: Float = 1.0f,
    val customThemes: List<CustomTheme> = emptyList(),
    val selectedCustomThemeId: String? = null,
    val isLoading: Boolean = false,
    val showCustomThemeEditor: Boolean = false,
    val editingTheme: CustomTheme? = null
) : UiState

/**
 * 主題設定 UI 事件
 */
sealed class ThemeSettingsUiEvent : UiEvent {
    data class SetThemeMode(val mode: ThemeMode) : ThemeSettingsUiEvent()
    data class SetPalette(val palette: ThemePalette) : ThemeSettingsUiEvent()
    data class SetDynamicColor(val enabled: Boolean) : ThemeSettingsUiEvent()
    data class SetAmoledBlack(val enabled: Boolean) : ThemeSettingsUiEvent()
    data class SetContrastLevel(val level: Float) : ThemeSettingsUiEvent()
    data class SelectCustomTheme(val themeId: String) : ThemeSettingsUiEvent()
    data class DeleteCustomTheme(val themeId: String) : ThemeSettingsUiEvent()
    object ShowCustomThemeEditor : ThemeSettingsUiEvent()
    object HideCustomThemeEditor : ThemeSettingsUiEvent()
    data class SaveCustomTheme(val theme: CustomTheme) : ThemeSettingsUiEvent()
    data class EditCustomTheme(val theme: CustomTheme) : ThemeSettingsUiEvent()
}

/**
 * 主題設定 UI 效果
 */
sealed class ThemeSettingsUiEffect : UiEffect {
    data class ShowMessage(val message: String) : ThemeSettingsUiEffect()
    object ThemeApplied : ThemeSettingsUiEffect()
}


