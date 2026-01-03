package com.pulse.music.core.designsystem

import androidx.compose.ui.unit.dp

/**
 * 統一的間距規範
 * 遵循 4dp 基準系統
 */
object PulseSpacing {
    // 基礎間距
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
    
    // 頁面邊距
    val screenPaddingHorizontal = 16.dp
    val screenPaddingVertical = 16.dp
    
    // 卡片間距
    val cardPadding = 16.dp
    val cardPaddingSmall = 12.dp
    val cardSpacing = 12.dp
    
    // 列表項間距
    val listItemSpacing = 8.dp
    val listItemPadding = 12.dp
    val listItemPaddingHorizontal = 16.dp
    val listItemPaddingVertical = 12.dp
    
    // Section 間距
    val sectionSpacing = 24.dp
    val sectionTitleSpacing = 12.dp
    
    // 底部安全區域 (for MiniPlayer, FAB etc.)
    val bottomSafeArea = 100.dp
}

/**
 * 統一的尺寸規範
 */
object PulseSize {
    // 圖標尺寸
    val iconXs = 16.dp
    val iconSm = 20.dp
    val iconMd = 24.dp
    val iconLg = 32.dp
    val iconXl = 48.dp
    val iconXxl = 64.dp
    
    // 專輯封面尺寸
    val albumArtThumbnail = 48.dp
    val albumArtSmall = 56.dp
    val albumArtMedium = 80.dp
    val albumArtLarge = 120.dp
    val albumArtXl = 200.dp
    val albumArtHero = 280.dp
    
    // 按鈕尺寸
    val buttonHeight = 48.dp
    val buttonHeightSmall = 36.dp
    val buttonHeightLarge = 56.dp
    val fabSize = 56.dp
    val fabSizeSmall = 40.dp
    
    // 列表項高度
    val listItemHeight = 72.dp
    val listItemHeightCompact = 56.dp
    val listItemHeightLarge = 88.dp
    
    // 頂部欄高度
    val topBarHeight = 56.dp
    val miniPlayerHeight = 64.dp
    
    // 底部導航高度
    val bottomNavHeight = 80.dp
}

/**
 * 統一的圓角規範
 */
object PulseCorners {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val full = 100.dp  // 用於圓形
    
    // 特定元素圓角
    val card = 16.dp
    val cardLarge = 24.dp
    val chip = 20.dp
    val button = 12.dp
    val albumArt = 12.dp
    val albumArtLarge = 16.dp
    val bottomSheet = 24.dp
    val dialog = 28.dp
}

/**
 * 統一的動畫時長
 */
object PulseDuration {
    const val instant = 100
    const val fast = 150
    const val normal = 250
    const val slow = 350
    const val verySlow = 500
    
    // 特定動畫
    const val ripple = 300
    const val transition = 300
    const val reveal = 400
    const val collapse = 250
}

/**
 * 統一的陰影 / 高度
 */
object PulseElevation {
    val none = 0.dp
    val xs = 1.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 16.dp
    
    // 特定元素
    val card = 2.dp
    val cardHovered = 4.dp
    val fab = 6.dp
    val bottomSheet = 16.dp
    val dialog = 24.dp
}
