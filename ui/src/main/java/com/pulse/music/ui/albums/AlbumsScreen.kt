package com.pulse.music.ui.albums

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.core.designsystem.PulseSpacing
import com.pulse.music.core.designsystem.component.PulseAlbumGridCard
import com.pulse.music.core.designsystem.component.PulseEmptyState
import com.pulse.music.core.designsystem.component.PulseTopBarWithBack
import com.pulse.music.ui.R

/**
 * 重新設計的專輯畫面 - 使用統一設計系統
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    onBackClick: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            PulseTopBarWithBack(
                title = stringResource(R.string.albums),
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.albums.isEmpty() && !uiState.isLoading) {
            PulseEmptyState(
                icon = Icons.Rounded.Album,
                title = stringResource(R.string.no_albums),
                subtitle = "尚未發現任何專輯"
            )
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
                    items = uiState.albums,
                    key = { it.id }
                ) { album ->
                    PulseAlbumGridCard(
                        title = album.title,
                        subtitle = album.artist,
                        artUri = album.artUri,
                        songCount = album.songCount,
                        onClick = { onAlbumClick(album.id) }
                    )
                }
            }
        }
    }
}


