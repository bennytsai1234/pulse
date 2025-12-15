package com.gemini.music.ui.nowplaying

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Slider
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.gemini.music.domain.model.LyricLine
import com.gemini.music.domain.model.RepeatMode
import com.gemini.music.ui.component.AddToPlaylistDialog
import com.gemini.music.ui.component.WaveformSeekBar
import com.gemini.music.ui.component.KaraokeLyrics

import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.scaleIn


import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    onAlbumClick: (albumId: Long) -> Unit = {},
    onInternalEqualizerClick: () -> Unit = {},
    onEditTagsClick: (songId: Long) -> Unit = {},
    onArtworkLoaded: (Bitmap?) -> Unit = {},
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // State management
    var showLyrics by rememberSaveable { mutableStateOf(false) }
    var showMoreOptions by rememberSaveable { mutableStateOf(false) }
    var showAddToPlaylist by rememberSaveable { mutableStateOf(false) }
    var showSleepTimer by rememberSaveable { mutableStateOf(false) }

    // Bottom Sheet for Options
    if (showMoreOptions) {
        ModalBottomSheet(
            onDismissRequest = { showMoreOptions = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF1E1E1E),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.5f)
                )
                
                // Option Items
                OptionItem(
                    icon = Icons.AutoMirrored.Rounded.QueueMusic,
                    text = "Add to Playlist",
                    onClick = { 
                        showMoreOptions = false
                        showAddToPlaylist = true
                    }
                )

                OptionItem(
                    icon = Icons.Rounded.Album,
                    text = "Go to Album",
                    onClick = { 
                        showMoreOptions = false
                        uiState.song?.albumId?.let { onAlbumClick(it) }
                    }
                )

                OptionItem(
                    icon = Icons.Rounded.GraphicEq,
                    text = "Equalizer",
                    onClick = {
                        showMoreOptions = false
                        onInternalEqualizerClick()
                    }
                )

                OptionItem(
                    icon = Icons.Rounded.Timer,
                    text = "Sleep Timer",
                    onClick = {
                        showMoreOptions = false
                        showSleepTimer = true
                    }
                )
                
                OptionItem(
                    icon = Icons.Rounded.Edit,
                    text = "Edit Tags",
                    onClick = {
                        showMoreOptions = false
                        uiState.song?.id?.let { onEditTagsClick(it) }
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Sheet Dialogs
    if (showSleepTimer) {
        com.gemini.music.ui.settings.SleepTimerDialog(
            onDismiss = { showSleepTimer = false },
            onSetTimer = { minutes ->
                viewModel.onEvent(NowPlayingEvent.SetSleepTimer(minutes))
                showSleepTimer = false
            },
            onCancelTimer = {
                viewModel.onEvent(NowPlayingEvent.CancelSleepTimer)
                showSleepTimer = false
            }
        )
    }

    if (showAddToPlaylist) {
        com.gemini.music.ui.component.AddToPlaylistDialog(
            playlists = uiState.playlists,
            onDismiss = { showAddToPlaylist = false },
            onPlaylistSelected = { playlist -> 
                viewModel.onEvent(NowPlayingEvent.AddToPlaylist(playlist.id))
                showAddToPlaylist = false
            },
            onCreateNewPlaylist = { name ->
                viewModel.onEvent(NowPlayingEvent.CreatePlaylistAndAdd(name))
            }
        )
    }

    // MAIN UI STRUCTURE
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Immersive Background (Blurred Art)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uiState.song?.albumArtUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(25.dp),
            alpha = 0.6f 
        )
        
        // 2. Gradient Overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f), // Top
                            Color.Black.copy(alpha = 0.6f), // Middle
                            Color.Black.copy(alpha = 0.95f) // Bottom (Controls)
                        )
                    )
                )
        )

        // 3. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Spacer(Modifier.weight(1f))
                
                IconButton(onClick = { showMoreOptions = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
            }

            // Expandable Content (Lyrics or Art)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (showLyrics) {
                    KaraokeLyrics(
                        lyrics = uiState.lyrics,
                        currentPosition = (uiState.progress * (uiState.song?.duration ?: 1L)).toLong(),
                        isLoading = uiState.lyricsLoading,
                        hasError = uiState.lyricsError,
                        onRetry = { viewModel.onEvent(NowPlayingEvent.RetryLoadLyrics) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    HeroImage(
                        artUri = uiState.song?.albumArtUri,
                        isPlaying = uiState.isPlaying,
                        onImageLoaded = onArtworkLoaded,
                        onClick = { showLyrics = !showLyrics },
                        onSwipeLeft = { viewModel.onEvent(NowPlayingEvent.SkipNext) },
                        onSwipeRight = { viewModel.onEvent(NowPlayingEvent.SkipPrevious) },
                        onDoubleTapLeft = { viewModel.onEvent(NowPlayingEvent.SeekBackward10s) },
                        onDoubleTapRight = { viewModel.onEvent(NowPlayingEvent.SeekForward10s) }
                    )
                }
            }

            // Bottom Control Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Song Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = uiState.song?.title ?: "Unknown Title",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = uiState.song?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Seek Bar (Waveform if available, else Slider)
                if (uiState.waveform.isNotEmpty()) {
                    WaveformSeekBar(
                        waveform = uiState.waveform,
                        progress = uiState.progress,
                        onValueChange = { viewModel.onEvent(NowPlayingEvent.SeekTo(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                } else {
                    Slider(
                        value = uiState.progress,
                        onValueChange = { viewModel.onEvent(NowPlayingEvent.SeekTo(it)) },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Time Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = uiState.currentTime, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                    Text(text = uiState.totalTime, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                }

                Spacer(Modifier.height(16.dp))

                // Playback Controls
                PlayerControls(
                    isPlaying = uiState.isPlaying,
                    onPlayPause = { viewModel.onEvent(NowPlayingEvent.PlayPauseToggle) },
                    onSkipNext = { viewModel.onEvent(NowPlayingEvent.SkipNext) },
                    onSkipPrevious = { viewModel.onEvent(NowPlayingEvent.SkipPrevious) },
                    shuffleEnabled = uiState.shuffleModeEnabled,
                    repeatMode = uiState.repeatMode,
                    onShuffleToggle = { viewModel.onEvent(NowPlayingEvent.ToggleShuffle) },
                    onRepeatToggle = { viewModel.onEvent(NowPlayingEvent.ToggleRepeat) },
                    isFavorite = uiState.isFavorite,
                    onFavoriteToggle = { viewModel.onEvent(NowPlayingEvent.ToggleFavorite) },
                    onQueueClick = onQueueClick
                )
            }
        }
    }
}

@Composable
fun OptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onQueueClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Button
        IconButton(onClick = onShuffleToggle) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Previous Button
        IconButton(onClick = onSkipPrevious) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        // Play/Pause Button
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
        }

        // Next Button
        IconButton(onClick = onSkipNext) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        // Repeat Button
        IconButton(onClick = onRepeatToggle) {
            val icon = when (repeatMode) {
                RepeatMode.OFF -> Icons.Rounded.Repeat
                RepeatMode.ALL -> Icons.Rounded.Repeat
                RepeatMode.ONE -> Icons.Rounded.RepeatOne
            }
            val tint = if (repeatMode == RepeatMode.OFF) Color.White.copy(alpha = 0.5f) else Color.White
            Icon(
                imageVector = icon,
                contentDescription = "Repeat",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favorite Button
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Red else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
        // Lyrics Button
        IconButton(onClick = { /* Handled by HeroImage click or separate button if needed */ }) {
            Icon(
                imageVector = Icons.Rounded.Description,
                contentDescription = "Lyrics",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
        // Queue Button
        IconButton(onClick = onQueueClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                contentDescription = "Queue",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TopControls(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onMoreClick: () -> Unit,
    isLyricsVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Collapse",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
             IconButton(onClick = onLyricsClick) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = "Lyrics",
                    tint = if (isLyricsVisible) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onQueueClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = "Queue",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More Options",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun HeroImage(
    artUri: String?,
    isPlaying: Boolean,
    onImageLoaded: (Bitmap?) -> Unit,
    onClick: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onDoubleTapLeft: () -> Unit = {},
    onDoubleTapRight: () -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 150, easing = androidx.compose.animation.core.LinearEasing),
        label = "ImageScale"
    )

    val animatedShadowElevation by animateFloatAsState(
        targetValue = if (isPlaying) 24f else 8f,
        animationSpec = tween(durationMillis = 150, easing = androidx.compose.animation.core.LinearEasing),
        label = "ShadowElevation"
    )
    
    // Swipe offset animation for visual feedback
    var swipeOffset by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = tween(durationMillis = 150),
        finishedListener = { swipeOffset = 0f },
        label = "SwipeOffset"
    )
    
    // Double tap hint indicators
    var showDoubleTapHintLeft by remember { mutableStateOf(false) }
    var showDoubleTapHintRight by remember { mutableStateOf(false) }
    
    val currentOnSwipeLeft by rememberUpdatedState(onSwipeLeft)
    val currentOnSwipeRight by rememberUpdatedState(onSwipeRight)
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnDoubleTapLeft by rememberUpdatedState(onDoubleTapLeft)
    val currentOnDoubleTapRight by rememberUpdatedState(onDoubleTapRight)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (kotlin.math.abs(swipeOffset) > 100) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (swipeOffset > 0) {
                                currentOnSwipeRight() // Previous
                            } else {
                                currentOnSwipeLeft() // Next
                            }
                        }
                        swipeOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeOffset = (swipeOffset + dragAmount).coerceIn(-200f, 200f)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { currentOnClick() },
                    onDoubleTap = { offset ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (offset.x < size.width / 2) {
                            currentOnDoubleTapLeft()
                            showDoubleTapHintLeft = true
                        } else {
                            currentOnDoubleTapRight()
                            showDoubleTapHintRight = true
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Shadow/Glow Background
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .scale(scale * 0.95f) // Slightly smaller than image
                .shadow(
                    elevation = animatedShadowElevation.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black,
                    ambientColor = Color.Black
                )
                .background(Color.Black, RoundedCornerShape(24.dp))
        )

        // Main Image
        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .scale(scale)
                .offset(x = animatedOffset.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(24.dp),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp) // Handled by modifier
        ) {
             AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { result ->
                    val resultBitmap = result.result.drawable.toBitmap()
                    onImageLoaded(resultBitmap)
                },
                onError = {
                     onImageLoaded(null)
                },
                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.Album)
            )
        }
        
        // Double Tap Indicators - Left
        androidx.compose.animation.AnimatedVisibility(
            visible = showDoubleTapHintLeft,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay10,
                    contentDescription = "-10s",
                    tint = Color.White
                )
            }
            // Auto hide hint
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(600)
                showDoubleTapHintLeft = false
            }
        }
        
        // Double Tap Indicators - Right
        androidx.compose.animation.AnimatedVisibility(
            visible = showDoubleTapHintRight,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 32.dp)
        ) {
             Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Forward10,
                    contentDescription = "+10s",
                    tint = Color.White
                )
            }
             // Auto hide hint
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(600)
                showDoubleTapHintRight = false
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongInfo(
    title: String,
    artist: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .basicMarquee()
            )
            
            Spacer(modifier = Modifier.size(16.dp))
            
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                    tint = if (isFavorite) Color.Red else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = artist,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp
            ),
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TimeControls(
    progress: Float,
    currentTime: String,
    totalTime: String,
    activeColor: Color,
    onSeek: (Float) -> Unit,
    waveform: List<Float>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        WaveformSeekBar(
            progress = progress,
            onValueChange = onSeek,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            activeColor = activeColor,
            inactiveColor = Color.White.copy(alpha = 0.3f),
            waveform = waveform
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = totalTime,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun MediaControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: RepeatMode,
    accentColor: Color,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween, // Spread out widely
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(onClick = onShuffleToggle) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleModeEnabled) accentColor else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(26.dp)
            )
        }

        // Previous
        IconButton(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPrev() 
            },
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        // Play/Pause - Premium Button
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = accentColor,
                    ambientColor = accentColor
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFE0E0E0)
                        )
                    )
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPlayPause()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black, // Or accentColor if distinct enough
                modifier = Modifier.size(36.dp)
            )
        }

        // Next
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onNext()
            },
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        // Repeat
        IconButton(onClick = onRepeatToggle) {
            val icon = when (repeatMode) {
                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                else -> Icons.Rounded.Repeat
            }
            val tint = if (repeatMode != RepeatMode.OFF) accentColor else Color.White.copy(alpha = 0.5f)
            
            Icon(
                imageVector = icon,
                contentDescription = "Repeat",
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    currentIndex: Int,
    onTap: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    var isUserScrolling by remember { mutableStateOf(false) }
    
    // Detect user interaction
    val isScrollInProgress = listState.isScrollInProgress
    LaunchedEffect(isScrollInProgress) {
        if (isScrollInProgress) {
            isUserScrolling = true
        } else {
            // Resume auto-scroll after 3 seconds of inactivity
            kotlinx.coroutines.delay(3000)
            isUserScrolling = false
        }
    }

    LaunchedEffect(currentIndex, isUserScrolling) {
        if (!isUserScrolling && currentIndex >= 0 && currentIndex < lyrics.size) {
            // Calculate offset to center the item roughly
            // We use a safe estimate or specific item positioning if possible.
            // Using animateScrollToItem with offset is the best we can do without complex layout measurement.
            // Offset 0 puts it at top. 
            // We want it in middle. Assuming standard screen height ~800dp, half is 400dp.
            // But we have padding. Let's aim for top-third for better readability.
            listState.animateScrollToItem(
                index = currentIndex,
                scrollOffset = -300 // Negative offset doesn't work as "from top" in LazyColumn standardly? 
                // Actually scrollToItem(index, scrollOffset) -> offset is pixels from top of visible area.
                // So positive offset pushes it DOWN.
                // We want to push it down to the middle.
            )
        }
    }

    if (lyrics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onTap),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Lyrics Found",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onTap),
                contentPadding = PaddingValues(top = 150.dp, bottom = 150.dp), // Add padding to allow start/end items to be centered
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isCurrent = index == currentIndex
                    // Active line: Opaque, Larger. Inactive: Faded, Smaller.
                    val targetAlpha = if (isCurrent) 1f else 0.4f
                    val targetScale = if (isCurrent) 1.1f else 0.95f
                    val targetColor = if (isCurrent) Color.White else Color.LightGray
                    
                    val alpha by animateFloatAsState(targetValue = targetAlpha, label = "alpha")
                    val scale by animateFloatAsState(targetValue = targetScale, label = "scale")
                    val color by animateColorAsState(targetValue = targetColor, label = "color")

                    Text(
                        text = line.text,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            shadow = if (isCurrent) androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(0f, 4f),
                                blurRadius = 8f
                            ) else null
                        ),
                        color = color.copy(alpha = alpha),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            // Seek to this line on click
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null // No ripple for clear reading
                            ) {
                                onSeek(line.startTime)
                            }
                            .padding(horizontal = 32.dp)
                    )
                }
            }
            
            // "Resume Sync" button if user scrolled away
            androidx.compose.animation.AnimatedVisibility(
                visible = isUserScrolling,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 24.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { isUserScrolling = false },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Resume Sync",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
