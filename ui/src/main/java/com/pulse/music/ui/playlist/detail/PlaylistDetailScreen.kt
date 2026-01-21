package com.pulse.music.ui.playlist.detail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pulse.music.core.designsystem.component.PulseEmptyState
import com.pulse.music.core.designsystem.component.PulseTopBarWithBack
import com.pulse.music.domain.model.Song
import com.pulse.music.ui.component.SongListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlaylistDetailScreen(
        viewModel: PlaylistDetailViewModel = hiltViewModel(),
        onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var isReorderMode by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        RenamePlaylistDialog(
                currentName = uiState.playlist?.name ?: "",
                onDismiss = { showRenameDialog = false },
                onConfirm = { newName ->
                    viewModel.renamePlaylist(newName)
                    showRenameDialog = false
                }
        )
    }

    Scaffold(
            topBar = {
                PulseTopBarWithBack(
                        title = uiState.playlist?.name ?: "Playlist",
                        onBackClick = onBackClick,
                        actions = {
                            // Reorder Toggle Button
                            if (uiState.songs.size > 1) {
                                TextButton(onClick = { isReorderMode = !isReorderMode }) {
                                    Text(if (isReorderMode) "Done" else "Edit")
                                }
                            }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                        text = { Text("Rename Playlist") },
                                        onClick = {
                                            showMenu = false
                                            showRenameDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Edit, contentDescription = null)
                                        }
                                )
                            }
                        }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.songs.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                PulseEmptyState(
                        icon = Icons.AutoMirrored.Rounded.QueueMusic,
                        title = "Empty Playlist",
                        subtitle = "Add songs from your library!"
                )
            }
        } else {
            ReorderableSongList(
                    songs = uiState.songs,
                    playlist = uiState.playlist,
                    isReorderMode = isReorderMode,
                    onSongClick = { viewModel.playSong(it) },
                    onRemoveSong = { viewModel.removeSong(it.id) },
                    onMoveSong = { from, to -> viewModel.moveSong(from, to) },
                    modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReorderableSongList(
        songs: List<Song>,
        playlist: com.pulse.music.domain.model.Playlist?,
        isReorderMode: Boolean,
        onSongClick: (Song) -> Unit,
        onRemoveSong: (Song) -> Unit,
        onMoveSong: (fromIndex: Int, toIndex: Int) -> Unit,
        modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    // Track dragging state
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggedOverIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            if (playlist != null) {
                PlaylistHeader(playlist = playlist)
            }
        }

        itemsIndexed(items = songs, key = { _, song -> song.id }) { index, song ->
            val isDragging = draggedItemIndex == index
            val isDraggedOver =
                    draggedOverIndex == index &&
                            draggedItemIndex != null &&
                            draggedItemIndex != index

            val elevation by
                    animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "DragElevation"
                    )

            val backgroundColor =
                    when {
                        isDragging -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        isDraggedOver -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> Color.Transparent
                    }

            if (isReorderMode) {
                // Reorder mode: show drag handle
                DraggableSongItem(
                        song = song,
                        index = index,
                        elevation = elevation,
                        backgroundColor = backgroundColor,
                        onDragStart = {
                            draggedItemIndex = index
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDragEnd = {
                            if (draggedItemIndex != null &&
                                            draggedOverIndex != null &&
                                            draggedItemIndex != draggedOverIndex
                            ) {
                                onMoveSong(draggedItemIndex!!, draggedOverIndex!!)
                            }
                            draggedItemIndex = null
                            draggedOverIndex = null
                        },
                        onDragOver = { targetIndex ->
                            if (targetIndex in songs.indices && targetIndex != draggedItemIndex) {
                                draggedOverIndex = targetIndex
                            }
                        },
                        totalItems = songs.size,
                        listState = listState,
                        scope = scope
                )
            } else {
                // Normal mode: swipe to dismiss
                SwipeToDeleteSongItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onRemove = { onRemoveSong(song) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteSongItem(song: Song, onClick: () -> Unit, onRemove: () -> Unit) {
    val dismissState =
            rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            onRemove()
                            true
                        } else {
                            false
                        }
                    }
            )

    SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            content = {
                SongListItem(
                        song = song,
                        onClick = onClick,
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                )
            }
    )
}

@Composable
private fun DraggableSongItem(
        song: Song,
        index: Int,
        elevation: androidx.compose.ui.unit.Dp,
        backgroundColor: Color,
        onDragStart: () -> Unit,
        onDragEnd: () -> Unit,
        onDragOver: (Int) -> Unit,
        totalItems: Int,
        listState: androidx.compose.foundation.lazy.LazyListState,
        scope: kotlinx.coroutines.CoroutineScope
) {
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .shadow(elevation = elevation)
                            .background(backgroundColor)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        // Drag Handle
        Icon(
                imageVector = Icons.Rounded.DragIndicator,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                        Modifier.padding(8.dp).size(24.dp).pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        accumulatedDrag = 0f
                                        onDragStart()
                                    },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { onDragEnd() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        accumulatedDrag += dragAmount.y

                                        // Calculate how many positions we've moved
                                        val itemHeight = 72f // Approximate height of a song item
                                        val positionOffset = (accumulatedDrag / itemHeight).toInt()
                                        val newTargetIndex =
                                                (index + positionOffset).coerceIn(0, totalItems - 1)

                                        onDragOver(newTargetIndex)

                                        // Auto-scroll if near edges
                                        val visibleItems = listState.layoutInfo.visibleItemsInfo
                                        if (visibleItems.isNotEmpty()) {
                                            val firstVisible = visibleItems.first().index
                                            val lastVisible = visibleItems.last().index

                                            if (newTargetIndex <= firstVisible + 1 &&
                                                            firstVisible > 0
                                            ) {
                                                scope.launch {
                                                    listState.animateScrollToItem(firstVisible - 1)
                                                }
                                            } else if (newTargetIndex >= lastVisible - 1 &&
                                                            lastVisible < totalItems - 1
                                            ) {
                                                scope.launch {
                                                    listState.animateScrollToItem(firstVisible + 1)
                                                }
                                            }
                                        }
                                    }
                            )
                        }
        )

        // Song Info
        SongListItem(
                song = song,
                onClick = { /* Disabled during reorder mode */},
                modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PlaylistHeader(playlist: com.pulse.music.domain.model.Playlist) {
    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        val coverArtUri = playlist.coverArtUri

        // Background Image (Blurred)
        if (coverArtUri != null) {
            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(coverArtUri)
                                    .crossfade(true)
                                    .size(500) // Lower res for blur
                                    .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                            Modifier.fillMaxSize().graphicsLayer {
                                alpha = 0.6f
                                renderEffect =
                                        if (android.os.Build.VERSION.SDK_INT >=
                                                        android.os.Build.VERSION_CODES.S
                                        ) {
                                            android.graphics.RenderEffect.createBlurEffect(
                                                            50f,
                                                            50f,
                                                            android.graphics.Shader.TileMode.MIRROR
                                                    )
                                                    .asComposeRenderEffect()
                                        } else {
                                            null
                                        }
                            }
            )
        }

        // Gradient Overlay
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors =
                                                        listOf(
                                                                Color.Transparent,
                                                                MaterialTheme.colorScheme.background
                                                                        .copy(alpha = 0.5f),
                                                                MaterialTheme.colorScheme.background
                                                        )
                                        )
                                )
        )

        // Content
        Row(
                modifier = Modifier.align(Alignment.BottomStart).padding(24.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Foreground Image
            if (coverArtUri != null) {
                AsyncImage(
                        model =
                                ImageRequest.Builder(LocalContext.current)
                                .data(coverArtUri)
                                .crossfade(true)
                                .size(300)
                                .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                                Modifier.size(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .shadow(8.dp, RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                        error =
                                androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                        Icons.AutoMirrored.Rounded.PlaylistPlay
                                ),
                        placeholder =
                                androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                        Icons.AutoMirrored.Rounded.PlaylistPlay
                                )
                )
            } else {
                Box(
                        modifier =
                                Modifier.size(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = "${playlist.songCount} songs",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun RenamePlaylistDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Rename Playlist") },
            text = {
                OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Name") },
                        singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                        onClick = { if (text.isNotBlank()) onConfirm(text) },
                        enabled = text.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


