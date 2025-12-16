package com.gemini.music.ui.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
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
import com.gemini.music.core.designsystem.GeminiCorners
import com.gemini.music.core.designsystem.GeminiSize
import com.gemini.music.core.designsystem.GeminiSpacing
import com.gemini.music.domain.model.Song
import java.util.Calendar

/**
 * Dashboard 風格的首頁頭部
 */
@Composable
fun HomeDashboardHeader(
    totalSongs: Int,
    recentlyAddedSongs: List<Song>,
    onPlaylistClick: () -> Unit,
    onAlbumsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onStatsClick: () -> Unit,
    onFoldersClick: () -> Unit,
    onRecentSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 問候語區域
        DashboardGreeting()
        
        Spacer(modifier = Modifier.height(GeminiSpacing.lg))
        
        // 快捷入口
        DashboardQuickAccess(
            onPlaylistClick = onPlaylistClick,
            onAlbumsClick = onAlbumsClick,
            onFavoritesClick = onFavoritesClick,
            onDiscoverClick = onDiscoverClick,
            onStatsClick = onStatsClick,
            onFoldersClick = onFoldersClick
        )
        
        Spacer(modifier = Modifier.height(GeminiSpacing.xl))
        
        // 統計卡片
        DashboardStats(totalSongs = totalSongs)
        
        Spacer(modifier = Modifier.height(GeminiSpacing.xl))
        
        // 最近新增
        if (recentlyAddedSongs.isNotEmpty()) {
            DashboardRecentlyAdded(
                songs = recentlyAddedSongs,
                onSongClick = onRecentSongClick
            )
            
            Spacer(modifier = Modifier.height(GeminiSpacing.lg))
        }
    }
}

/**
 * 問候語
 */
@Composable
private fun DashboardGreeting() {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    
    val (greeting, icon, gradientColors) = when (hour) {
        in 5..11 -> Triple(
            "早安",
            "☀️",
            listOf(Color(0xFFFFD54F), Color(0xFFFFA726))
        )
        in 12..17 -> Triple(
            "午安", 
            "🌤️",
            listOf(Color(0xFF64B5F6), Color(0xFF42A5F5))
        )
        in 18..21 -> Triple(
            "晚安",
            "🌙",
            listOf(Color(0xFF7C4DFF), Color(0xFF536DFE))
        )
        else -> Triple(
            "夜深了",
            "🌃",
            listOf(Color(0xFF5C6BC0), Color(0xFF3949AB))
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GeminiSpacing.screenPaddingHorizontal)
            .clip(RoundedCornerShape(GeminiCorners.cardLarge))
            .background(
                Brush.horizontalGradient(
                    colors = gradientColors.map { it.copy(alpha = 0.2f) }
                )
            )
            .padding(GeminiSpacing.lg)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displaySmall
            )
            
            Spacer(modifier = Modifier.width(GeminiSpacing.md))
            
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "來聽點音樂吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 快捷入口網格
 */
@Composable
private fun DashboardQuickAccess(
    onPlaylistClick: () -> Unit,
    onAlbumsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onStatsClick: () -> Unit,
    onFoldersClick: () -> Unit
) {
    val items = listOf(
        QuickAccessData(Icons.AutoMirrored.Rounded.QueueMusic, "播放清單", onPlaylistClick, Color(0xFF6C63FF)),
        QuickAccessData(Icons.Rounded.Album, "專輯", onAlbumsClick, Color(0xFF00BCD4)),
        QuickAccessData(Icons.Rounded.Favorite, "最愛", onFavoritesClick, Color(0xFFE91E63)),
        QuickAccessData(Icons.Rounded.Explore, "探索", onDiscoverClick, Color(0xFFFF9800)),
        QuickAccessData(Icons.Rounded.BarChart, "統計", onStatsClick, Color(0xFF4CAF50)),
        QuickAccessData(Icons.Rounded.Folder, "資料夾", onFoldersClick, Color(0xFF795548))
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GeminiSpacing.screenPaddingHorizontal),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.take(4).forEach { item ->
            QuickAccessButton(
                icon = item.icon,
                label = item.label,
                onClick = item.onClick,
                tint = item.tint,
                modifier = Modifier.weight(1f)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(GeminiSpacing.sm))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GeminiSpacing.screenPaddingHorizontal),
        horizontalArrangement = Arrangement.Center
    ) {
        items.drop(4).forEach { item ->
            QuickAccessButton(
                icon = item.icon,
                label = item.label,
                onClick = item.onClick,
                tint = item.tint,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}

private data class QuickAccessData(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val tint: Color
)

@Composable
private fun QuickAccessButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(GeminiCorners.lg))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(GeminiSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(GeminiCorners.md))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(GeminiSize.iconMd)
            )
        }
        
        Spacer(modifier = Modifier.height(GeminiSpacing.xs))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

/**
 * 統計卡片
 */
@Composable
private fun DashboardStats(totalSongs: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GeminiSpacing.screenPaddingHorizontal),
        horizontalArrangement = Arrangement.spacedBy(GeminiSpacing.md)
    ) {
        StatCard(
            title = "歌曲總數",
            value = totalSongs.toString(),
            icon = Icons.Rounded.MusicNote,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "今日播放",
            value = "0",  // TODO: 從統計中獲取
            icon = Icons.Rounded.PlayCircle,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(GeminiCorners.card)
    ) {
        Row(
            modifier = Modifier.padding(GeminiSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(GeminiCorners.sm))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(GeminiSpacing.md))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 最近新增區塊
 */
@Composable
private fun DashboardRecentlyAdded(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GeminiSpacing.screenPaddingHorizontal),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "最近新增",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            TextButton(onClick = { /* Show all */ }) {
                Text("查看全部")
            }
        }
        
        Spacer(modifier = Modifier.height(GeminiSpacing.sm))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = GeminiSpacing.screenPaddingHorizontal),
            horizontalArrangement = Arrangement.spacedBy(GeminiSpacing.md)
        ) {
            items(songs.take(10)) { song ->
                RecentlyAddedCard(
                    song = song,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
private fun RecentlyAddedCard(
    song: Song,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    Column(
        modifier = Modifier
            .width(130.dp)
            .scale(scale)
            .clip(RoundedCornerShape(GeminiCorners.lg))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(GeminiSpacing.xs)
    ) {
        // 封面
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(GeminiCorners.albumArtLarge)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // 播放按鈕覆層
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(GeminiSpacing.sm))
        
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
