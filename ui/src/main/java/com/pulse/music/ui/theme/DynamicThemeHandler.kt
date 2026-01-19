package com.pulse.music.ui.theme

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.pulse.music.ui.theme.LocalDynamicTheme
import kotlinx.coroutines.launch

@Composable
fun DynamicThemeHandler(
    artworkUri: String?,
    content: @Composable (seedColor: Color?) -> Unit
) {
    val dynamicTheme = LocalDynamicTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(artworkUri) {
        if (artworkUri != null) {
            scope.launch {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .allowHardware(false) // Software bitmap required for Palette
                    .build()
                
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.drawable.toBitmap()
                    dynamicTheme.extractColorsFromBitmap(bitmap)
                } else {
                    dynamicTheme.extractColorsFromBitmap(null)
                }
            }
        } else {
            dynamicTheme.extractColorsFromBitmap(null)
        }
    }

    // Return the dominant color as seed
    // If we want more complex mapping, we would construct a full scheme here
    // But PulseTheme now accepts a seedColor.
    // However, DynamicThemeState defaults to a Purple-ish dominant color.
    // We should check if it's default or extracted.
    // For now, let's just pass the dominant color.
    
    content(dynamicTheme.dominantColor)
}
