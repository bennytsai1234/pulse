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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    onAlbumClick: (albumId: Long) -> Unit = {},
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showLyrics by remember { mutableStateOf(false) }

    var showMoreOptions by remember { mutableStateOf(false) }
    
    var showAddToPlaylist by remember { mutableStateOf(false) }

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
                
                // Add to Playlist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            showMoreOptions = false
                            showAddToPlaylist = true
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = "Add to Playlist", style = MaterialTheme.typography.bodyLarge)
                }

                // Go to Album
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            showMoreOptions = false
                            uiState.song?.albumId?.let { albumId ->
                                onAlbumClick(albumId)
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Album,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = "Go to Album", style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddToPlaylist) {
        AddToPlaylistDialog(
            playlists = uiState.playlists,
            onDismiss = { showAddToPlaylist = false },
            onPlaylistSelected = { playlist ->
                viewModel.onEvent(NowPlayingEvent.AddToPlaylist(playlist.id))
                showAddToPlaylist = false
            },
            onCreateNewPlaylist = { name ->
                viewModel.onEvent(NowPlayingEvent.CreatePlaylistAndAdd(name))
                showAddToPlaylist = false
            }
        )
    }

    val startColor by animateColorAsState(
        targetValue = uiState.gradientColors.getOrElse(0) { Color(0xFF1E1E1E) },
        animationSpec = tween(durationMillis = 400),
        label = "StartColor"
    )
    val endColor by animateColorAsState(
        targetValue = uiState.gradientColors.getOrElse(1) { Color.Black },
        animationSpec = tween(durationMillis = 400),
        label = "EndColor"
    )

    // Use a clean, dynamic color gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Base fallback
    ) {
        // 1. Main Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(startColor, endColor)
                    )
                )
        )

        // 2. Subtle Radial Glow for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            startColor.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(0.5f, -0.2f), // Glow from top center
                        radius = 2000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp), // Reduce padding slightly for more space
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Control Bar
            TopControls(
                onBackClick = onBackClick,
                onQueueClick = onQueueClick,
                onLyricsClick = { showLyrics = !showLyrics },
                onMoreClick = { showMoreOptions = true },
                isLyricsVisible = showLyrics,
                modifier = Modifier.statusBarsPadding()
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // 2. Main Content
            AnimatedContent(
                targetState = showLyrics,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "ContentSwitcher",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { isLyrics ->
                if (isLyrics) {
                    // Use KaraokeLyrics for enhanced word-by-word highlighting
                    val currentPositionMs = ((uiState.song?.duration ?: 0L) * uiState.progress).toLong()
                    KaraokeLyrics(
                        lyrics = uiState.lyrics,
                        currentPosition = currentPositionMs,
                        highlightColor = uiState.backgroundColor,
                        normalColor = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showLyrics = false }
                    )
                } else {
                    HeroImage(
                        artUri = uiState.song?.albumArtUri,
                        isPlaying = uiState.isPlaying,
                        onImageLoaded = { bitmap ->
                            viewModel.onEvent(NowPlayingEvent.UpdatePalette(bitmap))
                        },
                        onClick = { showLyrics = true },
                        onSwipeLeft = { viewModel.onEvent(NowPlayingEvent.SkipNext) },
                        onSwipeRight = { viewModel.onEvent(NowPlayingEvent.SkipPrevious) },
                        onDoubleTapLeft = { viewModel.onEvent(NowPlayingEvent.SeekBackward10s) },
                        onDoubleTapRight = { viewModel.onEvent(NowPlayingEvent.SeekForward10s) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 3. Song Info
            // 3. Song Info (Centered and Larger)
            SongInfo(
                title = uiState.song?.title ?: "No Audio",
                artist = uiState.song?.artist ?: "Unknown Artist",
                isFavorite = uiState.isFavorite,
                onToggleFavorite = { viewModel.onEvent(NowPlayingEvent.ToggleFavorite) },
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Time Controls
            TimeControls(
                progress = uiState.progress,
                currentTime = uiState.currentTime,
                totalTime = uiState.totalTime,
                activeColor = uiState.backgroundColor,
                onSeek = { viewModel.onEvent(NowPlayingEvent.SeekTo(it)) },
                waveform = uiState.waveform
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Media Controls
            // 5. Media Controls (Refined)
            MediaControls(
                isPlaying = uiState.isPlaying,
                shuffleModeEnabled = uiState.shuffleModeEnabled,
                repeatMode = uiState.repeatMode,
                accentColor = uiState.backgroundColor, // Pass dynamic color
                onPlayPause = { viewModel.onEvent(NowPlayingEvent.PlayPauseToggle) },
                onNext = { viewModel.onEvent(NowPlayingEvent.SkipNext) },
                onPrev = { viewModel.onEvent(NowPlayingEvent.SkipPrevious) },
                onShuffleToggle = { viewModel.onEvent(NowPlayingEvent.ToggleShuffle) },
                onRepeatToggle = { viewModel.onEvent(NowPlayingEvent.ToggleRepeat) }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
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
    onImageLoaded: (Bitmap) -> Unit,
    onClick: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onDoubleTapLeft: () -> Unit = {},
    onDoubleTapRight: () -> Unit = {}
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 250),
        label = "ImageScale"
    )

    val animatedShadowElevation by animateFloatAsState(
        targetValue = if (isPlaying) 24f else 8f,
        animationSpec = tween(durationMillis = 250),
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
    
    // Double tap indicator
    var showDoubleTapIndicator by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(showDoubleTapIndicator) {
        if (showDoubleTapIndicator != null) {
            kotlinx.coroutines.delay(500)
            showDoubleTapIndicator = null
        }
    }
    
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
                                onSwipeRight() // Previous
                            } else {
                                onSwipeLeft() // Next
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
                    onTap = { offset ->
                        onClick()
                    },
                    onDoubleTap = { offset ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        val width = size.width
                        if (offset.x < width / 2) {
                            // Left side - go back 10 seconds
                            showDoubleTapIndicator = "-10s"
                            onDoubleTapLeft()
                        } else {
                            // Right side - skip 10 seconds
                            showDoubleTapIndicator = "+10s"
                            onDoubleTapRight()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Pseudo-glow layer
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = scale * 0.9f
                    scaleY = scale * 0.9f
                    alpha = 0.5f
                    translationX = animatedOffset * 0.3f
                }
                .blur(32.dp)
                .background(Color.White.copy(0.2f), CircleShape)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    shadowElevation = animatedShadowElevation
                    shape = RoundedCornerShape(28.dp)
                    clip = true
                    translationX = animatedOffset
                }
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artUri)
                    .allowHardware(false)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { result ->
                    val resultBitmap = result.result.drawable.toBitmap()
                    onImageLoaded(resultBitmap)
                },
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
            
            // Double Tap Indicator Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = showDoubleTapIndicator != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(
                    if (showDoubleTapIndicator == "-10s") Alignment.CenterStart 
                    else Alignment.CenterEnd
                )
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = showDoubleTapIndicator ?: "",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
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
