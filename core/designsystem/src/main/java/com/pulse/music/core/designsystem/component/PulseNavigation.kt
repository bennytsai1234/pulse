package com.pulse.music.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.util.Calendar

/**
 * 統一的頂部欄
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = { navigationIcon?.invoke() },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        modifier = modifier
    )
}

/**
 * 帶返回按鈕的頂部欄 - 緊湊型設計，與首頁 TopBar 高度一致
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseTopBarWithBack(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // 使用與 HomeScreenRedesigned 相同的緊湊型 Row 佈局
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "返回"
            )
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        // Actions
        actions()
    }
}

/**
 * 問候語標頭 (Dashboard 首頁用)
 */
@Composable
fun PULSEGreetingHeader(
    modifier: Modifier = Modifier,
    userName: String? = null
) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val greeting = when (hour) {
        in 5..11 -> "早安"
        in 12..17 -> "午安"
        in 18..21 -> "晚安"
        else -> "夜深了"
    }

    val icon = when (hour) {
        in 5..11 -> "☀️"
        in 12..17 -> "🌤️"
        in 18..21 -> "🌙"
        else -> "🌃"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = PulseSpacing.screenPaddingHorizontal,
                vertical = PulseSpacing.lg
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(PulseSpacing.sm))
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (userName != null) {
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(PulseSpacing.xs))

        Text(
            text = "來聽點音樂吧",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/**
 * Dashboard 快捷入口列
 */
@Composable
fun PULSEQuickAccessRow(
    items: List<QuickAccessItem>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = PulseSpacing.screenPaddingHorizontal),
        horizontalArrangement = Arrangement.spacedBy(PulseSpacing.md)
    ) {
        items(items) { item ->
            PULSEQuickAction(
                icon = item.icon,
                label = item.label,
                onClick = item.onClick,
                iconTint = item.iconTint ?: MaterialTheme.colorScheme.primary,
                badge = item.badge
            )
        }
    }
}

data class QuickAccessItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val iconTint: Color? = null,
    val badge: String? = null
)

/**
 * 統計卡片 (Dashboard 用)
 */
@Composable
fun PULSEStatsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    PulseCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(PulseSpacing.cardPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(PulseCorners.md))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(PulseSpacing.md))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(PulseSpacing.xxs))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 橫向滑動的專輯/歌曲卡片
 */
@Composable
fun PULSEHorizontalCard(
    title: String,
    subtitle: String,
    imageUri: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Column(
        modifier = modifier
            .width(140.dp)
            .scale(scale)
            .clip(RoundedCornerShape(PulseCorners.lg))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(PulseSpacing.sm)
    ) {
        // 封面圖
        Box(
            modifier = Modifier
                .size(124.dp)
                .clip(RoundedCornerShape(PulseCorners.albumArtLarge)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUri)
                        .crossfade(true)
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
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PulseSpacing.sm))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 篩選/排序 Chip
 */
@Composable
fun PULSEFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
                      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "content"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(PulseCorners.chip))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(PulseCorners.chip)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PulseSpacing.md,
                vertical = PulseSpacing.sm
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(PulseSize.iconXs)
                )
                Spacer(modifier = Modifier.width(PulseSpacing.xs))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

/**
 * 操作欄 (播放全部、隨機播放等)
 */
@Composable
fun PULSEControlRow(
    itemCount: Int,
    modifier: Modifier = Modifier,
    onPlayAll: (() -> Unit)? = null,
    onShuffle: (() -> Unit)? = null,
    filterChips: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = PulseSpacing.screenPaddingHorizontal,
                vertical = PulseSpacing.sm
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左側：數量與篩選
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
        ) {
            Text(
                text = "$itemCount 首",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            filterChips()
        }

        // 右側：播放按鈕
        Row(
            horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm)
        ) {
            if (onShuffle != null) {
                FilledTonalIconButton(
                    onClick = onShuffle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "隨機播放",
                        modifier = Modifier.size(PulseSize.iconSm)
                    )
                }
            }

            if (onPlayAll != null) {
                FilledIconButton(
                    onClick = onPlayAll,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "播放全部",
                        modifier = Modifier.size(PulseSize.iconMd)
                    )
                }
            }
        }
    }
}
