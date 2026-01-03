package com.pulse.music.ui.playlist.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.core.designsystem.PulseSpacing
import com.pulse.music.core.designsystem.component.PulseEmptyState
import com.pulse.music.core.designsystem.component.PulsePlaylistGridCard
import com.pulse.music.core.designsystem.component.PulseTopBarWithBack
import com.pulse.music.ui.component.CreatePlaylistDialog
import com.pulse.music.domain.model.Playlist

/**
 * 重新設計的播放清單畫面 - 使用統一設計系統
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistListScreen(
    viewModel: PlaylistListViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { name -> viewModel.createPlaylist(name) }
        )
    }

    if (uiState.playlistToRename != null) {
        CreatePlaylistDialog(
            initialName = uiState.playlistToRename!!.name,
            title = "Rename Playlist",
            confirmButtonText = "Rename",
            onDismiss = { viewModel.dismissRenameDialog() },
            onConfirm = { name -> viewModel.renamePlaylist(uiState.playlistToRename!!.id, name) }
        )
    }

    Scaffold(
        topBar = {
            PulseTopBarWithBack(
                title = "播放清單",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create Playlist")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.playlists.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                PulseEmptyState(
                    icon = Icons.AutoMirrored.Rounded.List,
                    title = "沒有播放清單",
                    subtitle = "建立你的第一個播放清單吧！"
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = PulseSpacing.screenPaddingHorizontal,
                    end = PulseSpacing.screenPaddingHorizontal,
                    top = PulseSpacing.md,
                    bottom = PulseSpacing.bottomSafeArea
                ),
                horizontalArrangement = Arrangement.spacedBy(PulseSpacing.cardSpacing),
                verticalArrangement = Arrangement.spacedBy(PulseSpacing.cardSpacing)
            ) {
                items(
                    items = uiState.playlists,
                    key = { it.id }
                ) { playlist ->
                    PlaylistGridItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistGridItem(
    playlist: Playlist,
    onClick: () -> Unit
) {
    // 取得封面 URI 列表 (最多 4 個用於 2x2 拼圖)
    val coverArts = listOfNotNull(playlist.coverArtUri)
    
    PulsePlaylistGridCard(
        title = playlist.name,
        songCount = playlist.songCount,
        coverArts = coverArts,
        onClick = onClick
    )
}


