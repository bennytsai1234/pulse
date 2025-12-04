package com.sigma.music.core.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sigma.music.core.datastore.UserPreferencesRepository
import com.sigma.music.core.designsystem.Typography

// We primarily focus on Dark Theme for this music player
private val DarkColorScheme = darkColorScheme(
    primary = SigmaPrimary,
    onPrimary = Color.White,
    primaryContainer = SigmaPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = SigmaAccent,
    onSecondary = Color.Black,
    background = SigmaBlack,
    onBackground = SigmaTextPrimary,
    surface = SigmaDarkGray,
    onSurface = SigmaTextPrimary,
    surfaceVariant = SigmaSurface,
    onSurfaceVariant = SigmaTextSecondary,
    error = SigmaError,
    outline = SigmaTextDisabled
)

// Fallback Light Scheme (Ideally user should stick to dark for music apps)
private val LightColorScheme = lightColorScheme(
    primary = SigmaPrimary,
    onPrimary = Color.White,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun SigmaTheme(
    userPreferencesRepository: UserPreferencesRepository,
    // Optional: Allow overriding for specific screens
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // We disable dynamic color by default to enforce our brand style
    content: @Composable () -> Unit
) {
    val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = UserPreferencesRepository.THEME_SYSTEM)
    
    val effectiveDarkTheme = when (themeMode) {
        UserPreferencesRepository.THEME_LIGHT -> false
        UserPreferencesRepository.THEME_DARK -> true
        else -> darkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (effectiveDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        effectiveDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color to match background for immersive feel
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb() // Or background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !effectiveDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Uses default for now, can customize later
        content = content
    )
}