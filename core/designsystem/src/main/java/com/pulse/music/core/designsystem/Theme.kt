package com.pulse.music.core.designsystem

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pulse.music.core.designsystem.PulseTypography

// We primarily focus on Dark Theme for this music player
private val DarkColorScheme = darkColorScheme(
    primary = PULSEPrimary,
    onPrimary = Color.White,
    primaryContainer = PULSEPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = PULSEAccent,
    onSecondary = Color.Black,
    background = PULSEBlack,
    onBackground = PULSETextPrimary,
    surface = PULSEDarkGray,
    onSurface = PULSETextPrimary,
    surfaceVariant = PULSESurface,
    onSurfaceVariant = PULSETextSecondary,
    error = PULSEError,
    outline = PULSETextDisabled
)

// Fallback Light Scheme (Ideally user should stick to dark for music apps)
private val LightColorScheme = lightColorScheme(
    primary = PULSEPrimary,
    onPrimary = Color.White,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun PulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // We disable dynamic color by default to enforce our brand style
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use WindowInsetsController for modern edge-to-edge setup
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PulseTypography, // Uses default for now, can customize later
        content = content
    )
}

