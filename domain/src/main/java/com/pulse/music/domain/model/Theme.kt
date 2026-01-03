package com.pulse.music.domain.model

import kotlinx.serialization.Serializable

/**
 * 應用程式主題類型
 */
enum class ThemeMode {
    SYSTEM,     // 跟隨系統
    LIGHT,      // 強制亮色
    DARK,       // 強制暗色
    AMOLED      // AMOLED 純黑 (省電)
}

/**
 * 預設主題調色盤
 */
enum class ThemePalette(
    val displayName: String,
    val primaryColor: Long,    // ARGB
    val accentColor: Long
) {
    PULSE("PULSE", 0xFF9C27B0, 0xFFE040FB),        // 紫色 (預設)
    OCEAN("Ocean", 0xFF0288D1, 0xFF4FC3F7),          // 海洋藍
    FOREST("Forest", 0xFF388E3C, 0xFF81C784),        // 森林綠
    SUNSET("Sunset", 0xFFE64A19, 0xFFFF8A65),        // 日落橙
    CHERRY("Cherry", 0xFFC2185B, 0xFFF48FB1),        // 櫻桃粉
    MIDNIGHT("Midnight", 0xFF1A237E, 0xFF7986CB),    // 午夜藍
    GOLD("Gold", 0xFFFF8F00, 0xFFFFD54F),            // 金色
    MONOCHROME("Monochrome", 0xFF616161, 0xFFBDBDBD) // 單色
}

/**
 * 自定義主題配置
 */
@Serializable
data class CustomTheme(
    val id: String,
    val name: String,
    val primaryColor: Long,
    val accentColor: Long,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val textPrimaryColor: Long,
    val textSecondaryColor: Long,
    val isDefault: Boolean = false
)

/**
 * 完整的主題設定
 */
@Serializable
data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,  // Android 12+ Material You
    val selectedPalette: ThemePalette = ThemePalette.PULSE,
    val customThemeId: String? = null,     // 若選用自定義主題
    val useAmoledBlack: Boolean = false,   // AMOLED 純黑背景
    val contrastLevel: Float = 1.0f        // 對比度調整 (0.8 - 1.2)
)

/**
 * 運行時的完整主題數據
 */
data class AppTheme(
    val settings: ThemeSettings,
    val isDarkMode: Boolean,
    val primaryColor: Long,
    val accentColor: Long,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val textPrimaryColor: Long,
    val textSecondaryColor: Long
)
