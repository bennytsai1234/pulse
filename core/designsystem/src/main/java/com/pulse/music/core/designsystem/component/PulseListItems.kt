package com.pulse.music.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pulse.music.core.designsystem.PulseCorners
import com.pulse.music.core.designsystem.PulseSize
import com.pulse.music.core.designsystem.PulseSpacing

/**
 * 統一的歌曲列表項目
 *
 * @param title 歌曲標題
 * @param subtitle 副標題 (藝人 - 專輯)
 * @param albumArtUri 專輯封面 URI
 * @param isPlaying 是否為當前播放歌曲
 * @param isFavorite 是否為最愛
 * @param duration 時長
 * @param showDuration 是否顯示時長
 * @param showFavorite 是否顯示最愛按鈕
 * @param showMoreButton 是否顯示更多按鈕
 * @param isSelected 是否被選中 (多選模式)
 * @param onClick 點擊回調
 * @param onLongClick 長按回調
 * @param onFavoriteClick 最愛按鈕回調
 * @param onMoreClick 更多按鈕回調
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PulseSongListItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    albumArtUri: String? = null,
    isPlaying: Boolean = false,
    isFavorite: Boolean = false,
    duration: String? = null,
    showDuration: Boolean = true,
    showFavorite: Boolean = false,
    showMoreButton: Boolean = false,
    isSelected: Boolean = false,
    showPlayingIndicator: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onFavoriteClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            isPlaying -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        label = "bg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(PulseSize.listItemHeight)
            .scale(scale)
            .clip(RoundedCornerShape(PulseCorners.md))
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                horizontal = PulseSpacing.listItemPaddingHorizontal,
                vertical = PulseSpacing.listItemPaddingVertical
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 專輯封面
        Box(
            modifier = Modifier
                .size(PulseSize.albumArtSmall)
                .clip(RoundedCornerShape(PulseCorners.albumArt)),
            contentAlignment = Alignment.Center
        ) {
            if (albumArtUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(albumArtUri)
                        .crossfade(true)
                        .memoryCacheKey(albumArtUri)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(PulseSize.iconMd)
                        )
                    }
                }
            }

            // 正在播放指示器
            if (isPlaying && showPlayingIndicator) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Playing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(PulseSize.iconMd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(PulseSpacing.md))

        // 文字內容
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isPlaying) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isPlaying) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(PulseSpacing.xxs))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (showDuration && duration != null) {
                    Text(
                        text = " · $duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 操作按鈕
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 最愛按鈕
            if (showFavorite) {
                IconButton(
                    onClick = { onFavoriteClick?.invoke() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(PulseSize.iconSm)
                    )
                }
            }

            // 更多按鈕
            if (showMoreButton) {
                IconButton(
                    onClick = { onMoreClick?.invoke() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(PulseSize.iconSm)
                    )
                }
            }
        }
    }
}

/**
 * 緊湊版歌曲列表項
 */
@Composable
fun PulseSongListItemCompact(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    albumArtUri: String? = null,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {}
) {
    PulseSongListItem(
        title = title,
        subtitle = subtitle,
        albumArtUri = albumArtUri,
        isPlaying = isPlaying,
        showDuration = false,
        showFavorite = false,
        showMoreButton = false,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * 專輯列表項
 */
@Composable
fun PULSEAlbumListItem(
    title: String,
    artist: String,
    songCount: Int,
    albumArtUri: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(PulseSize.listItemHeightLarge)
            .scale(scale)
            .clip(RoundedCornerShape(PulseCorners.md))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(
                horizontal = PulseSpacing.listItemPaddingHorizontal,
                vertical = PulseSpacing.md
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 專輯封面
        Box(
            modifier = Modifier
                .size(PulseSize.albumArtMedium)
                .clip(RoundedCornerShape(PulseCorners.albumArtLarge)),
            contentAlignment = Alignment.Center
        ) {
            if (albumArtUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(PulseSpacing.lg))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(PulseSpacing.xs))

            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(PulseSpacing.xxs))

            Text(
                text = "$songCount 首歌曲",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 藝人列表項
 */
@Composable
fun PULSEArtistListItem(
    name: String,
    songCount: Int,
    modifier: Modifier = Modifier,
    imageUri: String? = null,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(PulseSize.listItemHeight)
            .scale(scale)
            .clip(RoundedCornerShape(PulseCorners.md))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(
                horizontal = PulseSpacing.listItemPaddingHorizontal,
                vertical = PulseSpacing.listItemPaddingVertical
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圓形頭像
        Surface(
            modifier = Modifier.size(PulseSize.albumArtSmall),
            shape = RoundedCornerShape(PulseCorners.full),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(PulseSpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$songCount 首歌曲",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
