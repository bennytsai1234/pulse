package com.pulse.music.ui.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pulse.music.ui.navigation.LocalAnimatedContentScope
import com.pulse.music.ui.navigation.LocalSharedTransitionScope
import com.pulse.music.ui.navigation.SharedElementKeys

/**
 * 共享元素專輯封面
 * 在導航時會有流暢的共享過渡動畫
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedAlbumCover(
    albumId: Long,
    artUri: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalAnimatedContentScope.current
    
    val baseModifier = modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(MaterialTheme.colorScheme.surfaceVariant)
    
    // 如果共享過渡作用域可用，使用 sharedElement modifier
    val finalModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
        with(sharedTransitionScope) {
            baseModifier.sharedElement(
                state = rememberSharedContentState(key = SharedElementKeys.albumCover(albumId)),
                animatedVisibilityScope = animatedContentScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 150, easing = androidx.compose.animation.core.LinearEasing)
                }
            )
        }
    } else {
        baseModifier
    }
    
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(artUri)
            .crossfade(true)
            .size(500)
            .build(),
        contentDescription = "Album Cover",
        contentScale = ContentScale.Crop,
        modifier = finalModifier
    )
}

/**
 * 共享元素歌曲封面
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedSongCover(
    songId: Long,
    artUri: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalAnimatedContentScope.current
    
    val baseModifier = modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(MaterialTheme.colorScheme.surfaceVariant)
    
    val finalModifier = if (sharedTransitionScope != null && animatedContentScope != null) {
        with(sharedTransitionScope) {
            baseModifier.sharedElement(
                state = rememberSharedContentState(key = SharedElementKeys.songCover(songId)),
                animatedVisibilityScope = animatedContentScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 150, easing = androidx.compose.animation.core.LinearEasing)
                }
            )
        }
    } else {
        baseModifier
    }
    
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(artUri)
            .crossfade(true)
            .size(500)
            .build(),
        contentDescription = "Song Cover",
        contentScale = ContentScale.Crop,
        modifier = finalModifier
    )
}

/**
 * 用於容器動畫的 Modifier 擴展
 * 在過渡期間保持元素可見
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBounds(
    key: String,
    enter: Boolean = true
): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalAnimatedContentScope.current
    
    return if (sharedTransitionScope != null && animatedContentScope != null) {
        with(sharedTransitionScope) {
            this@sharedBounds.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedContentScope,
                enter = if (enter) androidx.compose.animation.fadeIn() else androidx.compose.animation.EnterTransition.None,
                exit = if (enter) androidx.compose.animation.fadeOut() else androidx.compose.animation.ExitTransition.None,
                boundsTransform = { _, _ -> tween(150, easing = androidx.compose.animation.core.LinearEasing) }
            )
        }
    } else {
        this
    }
}


