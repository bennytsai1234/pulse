package com.gemini.music.ui.theme

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 動態主題狀態管理器
 * 從專輯封面提取主題色並應用到整個 App
 */
@Stable
class DynamicThemeState {
    
    // Primary colors
    var dominantColor by mutableStateOf(DefaultColors.dominant)
        private set
    
    var accentColor by mutableStateOf(DefaultColors.accent)
        private set
    
    // Surface and background gradients
    var surfaceColor by mutableStateOf(DefaultColors.surface)
        private set
    
    var onSurfaceColor by mutableStateOf(Color.White)
        private set
    
    // Gradient colors for backgrounds
    var gradientStartColor by mutableStateOf(DefaultColors.gradientStart)
        private set
    
    var gradientEndColor by mutableStateOf(DefaultColors.gradientEnd)
        private set
    
    // Secondary accent for highlights
    var secondaryAccent by mutableStateOf(DefaultColors.secondaryAccent)
        private set
    
    var isLight by mutableStateOf(false)
        private set
    
    /**
     * 從 Bitmap 提取主題色
     */
    suspend fun extractColorsFromBitmap(bitmap: Bitmap?) = withContext(Dispatchers.Default) {
        if (bitmap == null) {
            resetToDefaults()
            return@withContext
        }
        
        try {
            val palette = Palette.from(bitmap).generate()
            
            // 優先順序：Vibrant > Muted > DominantSwatch
            val vibrant = palette.vibrantSwatch
            val muted = palette.mutedSwatch
            val darkMuted = palette.darkMutedSwatch
            val lightVibrant = palette.lightVibrantSwatch
            val lightMuted = palette.lightMutedSwatch
            val dominant = palette.dominantSwatch
            
            // Primary color selection
            val primarySwatch = vibrant ?: muted ?: dominant
            val accentSwatch = lightVibrant ?: lightMuted ?: vibrant
            val secondarySwatch = darkMuted ?: muted ?: dominant
            
            primarySwatch?.let { swatch ->
                val rgb = swatch.rgb
                dominantColor = Color(rgb)
                
                // 計算是否為亮色
                isLight = ColorUtils.calculateLuminance(rgb) > 0.5
                
                // 文字顏色
                onSurfaceColor = if (isLight) Color.Black else Color.White
                
                // Surface 使用暗化版本
                surfaceColor = Color(
                    ColorUtils.blendARGB(rgb, android.graphics.Color.BLACK, 0.75f)
                )
                
                // Gradient colors
                gradientStartColor = Color(
                    ColorUtils.blendARGB(rgb, android.graphics.Color.BLACK, 0.5f)
                )
                gradientEndColor = Color(
                    ColorUtils.blendARGB(rgb, android.graphics.Color.BLACK, 0.85f)
                )
            }
            
            accentSwatch?.let { swatch ->
                accentColor = Color(swatch.rgb)
            }
            
            secondarySwatch?.let { swatch ->
                secondaryAccent = Color(
                    ColorUtils.blendARGB(swatch.rgb, android.graphics.Color.WHITE, 0.3f)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            resetToDefaults()
        }
    }
    
    private fun resetToDefaults() {
        dominantColor = DefaultColors.dominant
        accentColor = DefaultColors.accent
        surfaceColor = DefaultColors.surface
        onSurfaceColor = Color.White
        gradientStartColor = DefaultColors.gradientStart
        gradientEndColor = DefaultColors.gradientEnd
        secondaryAccent = DefaultColors.secondaryAccent
        isLight = false
    }
    
    object DefaultColors {
        val dominant = Color(0xFF6C63FF)
        val accent = Color(0xFF00E5FF)
        val surface = Color(0xFF1E1E1E)
        val gradientStart = Color(0xFF2A2A2A)
        val gradientEnd = Color(0xFF121212)
        val secondaryAccent = Color(0xFF9C88FF)
    }
}

/**
 * Animated version of dynamic theme colors
 * Use in Composables for smooth color transitions
 */
@Composable
fun DynamicThemeState.animatedColors(): AnimatedDynamicColors {
    val animDominant by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(durationMillis = 500),
        label = "DominantColor"
    )
    val animAccent by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 500),
        label = "AccentColor"
    )
    val animSurface by animateColorAsState(
        targetValue = surfaceColor,
        animationSpec = tween(durationMillis = 500),
        label = "SurfaceColor"
    )
    val animOnSurface by animateColorAsState(
        targetValue = onSurfaceColor,
        animationSpec = tween(durationMillis = 500),
        label = "OnSurfaceColor"
    )
    val animGradientStart by animateColorAsState(
        targetValue = gradientStartColor,
        animationSpec = tween(durationMillis = 500),
        label = "GradientStart"
    )
    val animGradientEnd by animateColorAsState(
        targetValue = gradientEndColor,
        animationSpec = tween(durationMillis = 500),
        label = "GradientEnd"
    )
    val animSecondaryAccent by animateColorAsState(
        targetValue = secondaryAccent,
        animationSpec = tween(durationMillis = 500),
        label = "SecondaryAccent"
    )
    
    return AnimatedDynamicColors(
        dominant = animDominant,
        accent = animAccent,
        surface = animSurface,
        onSurface = animOnSurface,
        gradientStart = animGradientStart,
        gradientEnd = animGradientEnd,
        secondaryAccent = animSecondaryAccent
    )
}

@Stable
data class AnimatedDynamicColors(
    val dominant: Color,
    val accent: Color,
    val surface: Color,
    val onSurface: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val secondaryAccent: Color
)
