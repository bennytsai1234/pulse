package com.gemini.music.ui.favorites

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
import com.gemini.music.core.designsystem.GeminiSpacing
import com.gemini.music.core.designsystem.component.GeminiEmptyState
import com.gemini.music.core.designsystem.component.GeminiSongListItem
import com.gemini.music.core.designsystem.component.GeminiTopBarWithBack

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
            GeminiTopBarWithBack(
                title = "我的最愛",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.songs.isEmpty() && !uiState.isLoading) {
            GeminiEmptyState(
                icon = Icons.Rounded.Favorite,
                title = "尚無最愛歌曲",
                subtitle = "點擊歌曲旁的愛心圖標來加入最愛"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = GeminiSpacing.bottomSafeArea)
            ) {
                itemsIndexed(
                    items = uiState.songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    GeminiSongListItem(
                        title = song.title,
                        subtitle = "${song.artist} · ${song.album}",
                        albumArtUri = song.albumArtUri,
                        isPlaying = false, // TODO: 可連接播放狀態
                        isFavorite = true, // 在最愛頁面顯示已收藏
                        duration = formatDuration(song.duration),
                        showDuration = true,
                        showFavorite = true,
                        onClick = { viewModel.onSongClick(index) },
                        onFavoriteClick = { viewModel.toggleFavorite(song.id) },
                        modifier = Modifier.padding(horizontal = GeminiSpacing.xs)
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
    return String.format("%d:%02d", minutes, seconds)
}
