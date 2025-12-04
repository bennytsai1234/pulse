package com.sigma.music.ui.nowplaying

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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.sigma.music.domain.model.LyricLine
import com.sigma.music.domain.model.RepeatMode
import com.sigma.music.ui.component.WaveformSeekBar

import androidx.compose.material.icons.automirrored.rounded.QueueMusic

@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showLyrics by remember { mutableStateOf(false) }

    val animatedColor by animateColorAsState(
        targetValue = uiState.backgroundColor,
        animationSpec = tween(durationMillis = 800),
        label = "BackgroundColorAnimation"
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            animatedColor.copy(alpha = 0.6f),
            Color.Black
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Control Bar (With Status Bar Padding)
            TopControls(
                onBackClick = onBackClick,
                onQueueClick = onQueueClick,
                onMoreClick = { /* TODO */ },
                modifier = Modifier.statusBarsPadding()
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // 2. Main Content
            AnimatedContent(
                targetState = showLyrics,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "ContentSwitcher",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { isLyrics ->
                if (isLyrics) {
                    LyricsView(
                        lyrics = uiState.lyrics,
                        currentIndex = uiState.currentLyricIndex,
                        onTap = { showLyrics = false }
                    )
                } else {
                    HeroImage(
                        artUri = uiState.song?.albumArtUri,
                        isPlaying = uiState.isPlaying,
                        onImageLoaded = { bitmap ->
                            viewModel.onEvent(NowPlayingEvent.UpdatePalette(bitmap))
                        },
                        onClick = { showLyrics = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 3. Song Info
            SongInfo(
                title = uiState.song?.title ?: "No Audio",
                artist = uiState.song?.artist ?: "Unknown Artist"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Time Controls
            TimeControls(
                progress = uiState.progress,
                currentTime = uiState.currentTime,
                totalTime = uiState.totalTime,
                activeColor = Color.White,
                onSeek = { viewModel.onEvent(NowPlayingEvent.SeekTo(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Media Controls
            MediaControls(
                isPlaying = uiState.isPlaying,
                shuffleModeEnabled = uiState.shuffleModeEnabled,
                repeatMode = uiState.repeatMode,
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
    onMoreClick: () -> Unit,
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

// ... Rest of the file (HeroImage, SongInfo, TimeControls, MediaControls, LyricsView) remains same ...
// I will copy them to ensure file integrity.

@Composable
fun HeroImage(
    artUri: String?,
    isPlaying: Boolean,
    onImageLoaded: (Bitmap) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 500),
        label = "ImageScale"
    )

    val shadowElevation by animateFloatAsState(
        targetValue = if (isPlaying) 24f else 8f,
        animationSpec = tween(durationMillis = 500),
        label = "ShadowElevation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = shadowElevation.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black,
                    ambientColor = Color.Black
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Color.DarkGray)
                .clickable(onClick = onClick)
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
                    val resultBitmap = (result.result as SuccessResult).drawable.toBitmap()
                    onImageLoaded(resultBitmap)
                },
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongInfo(
    title: String,
    artist: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist,
            style = MaterialTheme.typography.titleMedium,
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
    onSeek: (Float) -> Unit
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
            inactiveColor = Color.White.copy(alpha = 0.3f)
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
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffleToggle) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleModeEnabled) Color.Green else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPrev() 
            },
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPlayPause()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
        }

        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onNext()
            },
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(onClick = onRepeatToggle) {
            val icon = when (repeatMode) {
                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                else -> Icons.Rounded.Repeat
            }
            val tint = if (repeatMode != RepeatMode.OFF) Color.Green else Color.White.copy(alpha = 0.6f)
            
            Icon(
                imageVector = icon,
                contentDescription = "Repeat",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun LyricsView(
    lyrics: List<LyricLine>,
    currentIndex: Int,
    onTap: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && currentIndex < lyrics.size) {
            listState.animateScrollToItem(
                index = currentIndex,
                scrollOffset = -500
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onTap),
            contentPadding = PaddingValues(vertical = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isCurrent = index == currentIndex
                val targetAlpha = if (isCurrent) 1f else 0.3f
                val targetScale = if (isCurrent) 1.2f else 0.95f
                
                val alpha by animateFloatAsState(targetValue = targetAlpha, label = "alpha")
                val scale by animateFloatAsState(targetValue = targetScale, label = "scale")

                Text(
                    text = line.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White.copy(alpha = alpha),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
    }
}