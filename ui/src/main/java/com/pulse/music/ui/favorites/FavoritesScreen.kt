package com.pulse.music.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.core.designsystem.PulseSpacing
import com.pulse.music.core.designsystem.component.PulseEmptyState
import com.pulse.music.core.designsystem.component.PulseSongListItem
import com.pulse.music.core.designsystem.component.PulseTopBarWithBack

/**
 * 重新設計的最愛畫面 - 使用統一設計系統
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            PulseTopBarWithBack(
                title = "我的最愛",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.songs.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                PulseEmptyState(
                    icon = Icons.Rounded.Favorite,
                    title = "尚無最愛歌曲",
                    subtitle = "點擊歌曲旁的愛心圖標來加入最愛"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = PulseSpacing.bottomSafeArea)
            ) {
                itemsIndexed(
                    items = uiState.songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    PulseSongListItem(
                        title = song.title,
                        subtitle = "${song.artist} · ${song.album}",
                        albumArtUri = song.albumArtUri,
                        isPlaying = false,
                        // Play functionality is handled by viewModel.onSongClick(index) which triggers playback
                        isFavorite = true, // 在最愛頁面顯示已收藏
                        duration = formatDuration(song.duration),
                        showDuration = true,
                        showFavorite = true,
                        onClick = { viewModel.onSongClick(index) },
                        onFavoriteClick = { viewModel.toggleFavorite(song.id) },
                        modifier = Modifier.padding(horizontal = PulseSpacing.xs)
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.ROOT, "%d:%02d", minutes, seconds)
}


