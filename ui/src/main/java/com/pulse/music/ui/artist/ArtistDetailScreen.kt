package com.pulse.music.ui.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pulse.music.core.designsystem.component.PulseTopBarWithBack
import com.pulse.music.ui.component.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
        onBackClick: () -> Unit,
        onAlbumClick: (Long) -> Unit,
        viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            topBar = { PulseTopBarWithBack(title = uiState.artistName, onBackClick = onBackClick) }
    ) { padding ->
        LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Artist Header
            item {
                ArtistHeader(
                        songCount = uiState.songs.size,
                        albumCount = uiState.albums.size,
                        coverArtUri = uiState.songs.firstOrNull()?.albumArtUri,
                        onPlayAll = { viewModel.playAll() },
                        onShuffle = { viewModel.shuffleAll() }
                )
            }

            // Albums Section
            if (uiState.albums.isNotEmpty()) {
                item {
                    Text(
                            text = "Albums",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                item {
                    LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.albums) { album ->
                            AlbumGridItem(
                                    albumName = album.title,
                                    albumArtUri = album.artUri,
                                    year = null, // Album doesn't have year field
                                    onClick = { onAlbumClick(album.id) }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Songs Section
            item {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Songs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = "${uiState.songs.size} songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            itemsIndexed(items = uiState.songs, key = { _, song -> song.id }) { index, song ->
                SongListItem(
                        song = song,
                        isSelected = false,
                        isSelectionMode = false,
                        onClick = { viewModel.playSong(index) },
                        onLongClick = {}
                )
            }
        }
    }
}

@Composable
private fun ArtistHeader(
        songCount: Int,
        albumCount: Int,
        coverArtUri: String?,
        onPlayAll: () -> Unit,
        onShuffle: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        // Blurred Background
        AsyncImage(
                model =
                        ImageRequest.Builder(LocalContext.current)
                                .data(coverArtUri)
                                .crossfade(true)
                                .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                        Modifier.fillMaxSize()
                                .blur(50.dp)
                                .background(
                                        MaterialTheme.colorScheme.surfaceVariant
                                ), // Fallback background color
                error =
                        androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                Icons.Rounded.Person
                        ),
                placeholder =
                        androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                Icons.Rounded.Person
                        )
        )

        // Gradient Overlay
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        Brush.verticalGradient(
                                                colors =
                                                        listOf(
                                                                Color.Transparent,
                                                                MaterialTheme.colorScheme.background
                                                                        .copy(alpha = 0.7f),
                                                                MaterialTheme.colorScheme.background
                                                        )
                                        )
                                )
        )

        // Content
        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            // Artist Avatar
            Card(
                    shape = CircleShape,
                    modifier = Modifier.size(100.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (coverArtUri != null) {
                        AsyncImage(
                                model =
                                        ImageRequest.Builder(LocalContext.current)
                                                .data(coverArtUri)
                                                .crossfade(true)
                                                .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error =
                                        androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                                Icons.Rounded.Person
                                        ),
                                placeholder =
                                        androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                                Icons.Rounded.Person
                                        )
                        )
                    } else {
                        Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Text(
                    text = "$songCount songs • $albumCount albums",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                        onClick = onPlayAll,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play All")
                }

                Button(
                        onClick = onShuffle,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor =
                                                MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor =
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                )
                ) {
                    Icon(
                            Icons.Rounded.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shuffle")
                }
            }
        }
    }
}

@Composable
private fun AlbumGridItem(
        albumName: String,
        albumArtUri: String?,
        year: Int?,
        onClick: () -> Unit
) {
    Column(modifier = Modifier.width(140.dp).clickable { onClick() }) {
        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.size(140.dp)) {
            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(albumArtUri)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                            Modifier.fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                    error =
                            androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                    Icons.Rounded.Album
                            ),
                    placeholder =
                            androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                    Icons.Rounded.Album
                            )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
                text = albumName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
        )

        year?.let {
            Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


