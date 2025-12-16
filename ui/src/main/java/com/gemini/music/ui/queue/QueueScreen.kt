package com.gemini.music.ui.queue

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gemini.music.domain.model.Song
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBackClick: () -> Unit,
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var isReorderMode by remember { mutableStateOf(false) }

    // Auto-scroll to current song when opening screen
    LaunchedEffect(Unit) {
        if (uiState.currentSongIndex >= 0) {
            listState.scrollToItem(uiState.currentSongIndex)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Up Next",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if (uiState.queue.size > 1) {
                        TextButton(onClick = { isReorderMode = !isReorderMode }) {
                            Text(if (isReorderMode) "Done" else "Sort")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = "Queue is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add songs to play next",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            val scope = rememberCoroutineScope()
            val hapticFeedback = LocalHapticFeedback.current
            
            // Track dragging state
            var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
            var draggedOverIndex by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                itemsIndexed(
                    items = uiState.queue,
                    key = { index, song -> "${song.id}_$index" }
                ) { index, song ->
                    val isCurrent = index == uiState.currentSongIndex
                    
                    if (isReorderMode) {
                        val isDragging = draggedItemIndex == index
                        val isDraggedOver = draggedOverIndex == index && draggedItemIndex != null && draggedItemIndex != index
                        
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "DragElevation"
                        )
                        
                        val backgroundColor = when {
                            isDragging -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            isDraggedOver -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else -> if (isCurrent) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
                        }

                        DraggableQueueItem(
                            song = song,
                            isCurrent = isCurrent,
                            isPlaying = uiState.isPlaying && isCurrent,
                            elevation = elevation,
                            backgroundColor = backgroundColor,
                            onDragStart = {
                                draggedItemIndex = index
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragEnd = {
                                if (draggedItemIndex != null && draggedOverIndex != null && draggedItemIndex != draggedOverIndex) {
                                    viewModel.moveItem(draggedItemIndex!!, draggedOverIndex!!)
                                }
                                draggedItemIndex = null
                                draggedOverIndex = null
                            },
                            onDragOver = { targetIndex ->
                                if (targetIndex in uiState.queue.indices && targetIndex != draggedItemIndex) {
                                    draggedOverIndex = targetIndex
                                }
                            },
                            totalItems = uiState.queue.size,
                            listState = listState,
                            scope = scope,
                            index = index // Pass index here
                        )
                    } else {
                                                    // Swipe to Dismiss Mode
                                                val dismissState = rememberSwipeToDismissBoxState(
                                                    confirmValueChange = {
                                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                                            viewModel.removeItem(index)
                                                            true
                                                        } else {
                                                            false
                                                        }
                                                    },
                                                    positionalThreshold = { it * 0.5f } // Require 50% swipe
                                                )
                        
                                                SwipeToDismissBox(
                                                    state = dismissState,
                                                    backgroundContent = {
                                                        val color by animateColorAsState(
                                                            targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 
                                                                MaterialTheme.colorScheme.errorContainer 
                                                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                            label = "DismissColor"
                                                        )
                                                        
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(color)
                                                                .padding(horizontal = 24.dp),
                                                            contentAlignment = Alignment.CenterEnd
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Close,
                                                                contentDescription = "Remove",
                                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                                            )
                                                        }
                                                    },
                                                    content = {
                                                        QueueItem(
                                                            song = song,
                                                            isCurrent = isCurrent,
                                                            isPlaying = uiState.isPlaying && isCurrent,
                                                            onClick = { viewModel.playItem(index) }
                                                        )
                                                    },
                                                    enableDismissFromStartToEnd = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        @Composable
                        fun QueueItem(
                            song: Song,
                            isCurrent: Boolean,
                            isPlaying: Boolean,
                            onClick: () -> Unit
                        ) {
                            val containerColor = if (isCurrent) {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = "${song.artist} • ${song.album}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingContent = {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(song.albumArtUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.Album),
                                        placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.Album)
                                    )
                                },
                                trailingContent = {
                                    if (isCurrent && isPlaying) {
                                        Icon(
                                            imageVector = Icons.Rounded.GraphicEq,
                                            contentDescription = "Playing",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = formatDuration(song.duration),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = containerColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onClick)
                            )
                        }
                        
@Composable
fun DraggableQueueItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    elevation: androidx.compose.ui.unit.Dp,
    backgroundColor: Color,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragOver: (Int) -> Unit,
    totalItems: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    scope: kotlinx.coroutines.CoroutineScope,
    index: Int // Added index parameter
) {
    var accumulatedDrag by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = elevation)
            .background(backgroundColor)
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Drag Handle
        Icon(
            imageVector = Icons.Rounded.DragIndicator,
            contentDescription = "Drag to reorder",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            accumulatedDrag = 0f
                            onDragStart()
                        },
                        onDragEnd = {
                            onDragEnd()
                        },
                        onDragCancel = {
                            onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount.y
                            
                            val itemHeight = 72.dp.toPx() // Approximate height
                            val positionOffset = (accumulatedDrag / itemHeight).toInt()
                            val targetIndex = (index + positionOffset).coerceIn(0, totalItems - 1)
                            
                            if (targetIndex != index) {
                                onDragOver(targetIndex)
                            }
                            
                            // Auto-scroll
                            val visibleItems = listState.layoutInfo.visibleItemsInfo
                            if (visibleItems.isNotEmpty()) {
                                val firstVisible = visibleItems.first().index
                                val lastVisible = visibleItems.last().index
                                
                                if (targetIndex <= firstVisible + 1 && firstVisible > 0) {
                                    scope.launch { listState.animateScrollToItem(firstVisible - 1) }
                                } else if (targetIndex >= lastVisible - 1 && lastVisible < totalItems - 1) {
                                    scope.launch { listState.animateScrollToItem(firstVisible + 1) }
                                }
                            }
                        }
                    )
                }
        )
        
        // Content
        QueueItem(
            song = song,
            isCurrent = isCurrent,
            isPlaying = isPlaying,
            onClick = {} 
        )
    }
}
private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

