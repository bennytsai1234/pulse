package com.pulse.music.ui.nowplaying

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.ui.component.KaraokeLyrics
import com.pulse.music.ui.component.WaveformSeekBar
import com.pulse.music.ui.nowplaying.components.HeroImage
import com.pulse.music.ui.nowplaying.components.OptionItem
import com.pulse.music.ui.nowplaying.components.PlayerControls

/**
 * Now Playing Screen - Main music player interface
 *
 * Displays:
 * - Album artwork with gesture controls
 * - Song information (title, artist)
 * - Playback controls (play/pause, skip, shuffle, repeat)
 * - Progress bar / Waveform seek bar
 * - Lyrics view (toggleable)
 *
 * @param onBackClick Callback when back/collapse button is pressed
 * @param onQueueClick Callback when queue button is pressed
 * @param onAlbumClick Callback when navigating to album, with album ID
 * @param onInternalEqualizerClick Callback when equalizer is opened
 * @param onEditTagsClick Callback when editing tags, with song ID
 * @param onEditLyricsClick Callback when editing lyrics, with song ID
 * @param onArtworkLoaded Callback when album artwork is loaded, returns Bitmap for color extraction
 * @param viewModel NowPlayingViewModel instance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    onAlbumClick: (albumId: Long) -> Unit = {},
    onInternalEqualizerClick: () -> Unit = {},
    onEditTagsClick: (songId: Long) -> Unit = {},
    onEditLyricsClick: (songId: Long) -> Unit = {},
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
        OptionsBottomSheet(
            onDismiss = { showMoreOptions = false },
            onAddToPlaylist = {
                showMoreOptions = false
                showAddToPlaylist = true
            },
            onGoToAlbum = {
                showMoreOptions = false
                uiState.song?.albumId?.let { onAlbumClick(it) }
            },
            onEqualizer = {
                showMoreOptions = false
                onInternalEqualizerClick()
            },
            onSleepTimer = {
                showMoreOptions = false
                showSleepTimer = true
            },
            onEditTags = {
                showMoreOptions = false
                uiState.song?.id?.let { onEditTagsClick(it) }
            },
            onEditLyrics = {
                showMoreOptions = false
                uiState.song?.id?.let { onEditLyricsClick(it) }
            }
        )
    }

    // Sheet Dialogs
    if (showSleepTimer) {
        com.pulse.music.ui.settings.SleepTimerDialog(
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
        com.pulse.music.ui.component.AddToPlaylistDialog(
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

    // MAIN UI STRUCTURE - Material You 風格 with Material3 background
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlassmorphicBackground(
            colors = uiState.gradientColors
        )

        // Create a dynamic color scheme for the content to ensure contrast
        // We override onSurface and onSurfaceVariant to match the extracted 'on' color
        // Keep primary vibrant for buttons and ensure onPrimary has proper contrast
        val dynamicContentColor = uiState.onBackgroundColor
        val vibrantColor = uiState.gradientColors.firstOrNull() ?: MaterialTheme.colorScheme.primary
        val dynamicColorScheme = MaterialTheme.colorScheme.copy(
            onSurface = dynamicContentColor,
            onSurfaceVariant = dynamicContentColor.copy(alpha = 0.7f),
            primary = vibrantColor,  // Use extracted vibrant color for buttons
            onPrimary = Color.White,  // Ensure high contrast for icons on primary
            outline = dynamicContentColor.copy(alpha = 0.5f)
        )

        MaterialTheme(colorScheme = dynamicColorScheme) {
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                NowPlayingTopBar(
                    onBackClick = onBackClick,
                    onMoreClick = { showMoreOptions = true }
                )

                // Expandable Content (Lyrics or Art)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (showLyrics) {
                        KaraokeLyrics(
                            lyrics = uiState.lyrics,
                            currentPosition = (uiState.progress * (uiState.song?.duration
                                ?: 1L)).toLong(),
                            isLoading = uiState.lyricsLoading,
                            hasError = uiState.lyricsError,
                            onRetry = { viewModel.onEvent(NowPlayingEvent.RetryLoadLyrics) },
                            onClick = { showLyrics = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        HeroImage(
                            artUri = uiState.song?.albumArtUri,
                            isPlaying = uiState.isPlaying,
                            onImageLoaded = { bitmap ->
                                viewModel.onEvent(NowPlayingEvent.UpdatePalette(bitmap))
                                onArtworkLoaded(bitmap)
                            },
                            onClick = { showLyrics = !showLyrics },
                            onSwipeLeft = { viewModel.onEvent(NowPlayingEvent.SkipNext) },
                            onSwipeRight = { viewModel.onEvent(NowPlayingEvent.SkipPrevious) },
                            onSwipeDown = onBackClick,
                            onDoubleTapLeft = {
                                viewModel.onEvent(NowPlayingEvent.SeekBackward10s)
                            },
                            onDoubleTapRight = {
                                viewModel.onEvent(NowPlayingEvent.SeekForward10s)
                            }
                        )
                    }
                }

                // Bottom Control Section
                NowPlayingBottomSection(
                    uiState = uiState,
                    onSeek = { viewModel.onEvent(NowPlayingEvent.SeekTo(it)) },
                    onPlayPause = { viewModel.onEvent(NowPlayingEvent.PlayPauseToggle) },
                    onSkipNext = { viewModel.onEvent(NowPlayingEvent.SkipNext) },
                    onSkipPrevious = { viewModel.onEvent(NowPlayingEvent.SkipPrevious) },
                    onShuffleToggle = { viewModel.onEvent(NowPlayingEvent.ToggleShuffle) },
                    onRepeatToggle = { viewModel.onEvent(NowPlayingEvent.ToggleRepeat) },
                    onFavoriteToggle = { viewModel.onEvent(NowPlayingEvent.ToggleFavorite) },
                    onQueueClick = onQueueClick,
                    onLyricsClick = { showLyrics = !showLyrics },
                    showLyrics = showLyrics
                )
            }
        }
    }
}

/**
 * Top bar with back button and options
 */
@Composable
private fun NowPlayingTopBar(
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit
) {
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
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "NOW PLAYING",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Bottom section with song info, seek bar, and playback controls
 */
@Composable
private fun NowPlayingBottomSection(
    uiState: NowPlayingUiState,
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit = {},
    showLyrics: Boolean = false
) {
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = uiState.song?.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                onValueChange = onSeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        } else {
            Slider(
                value = uiState.progress,
                onValueChange = onSeek,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = uiState.currentTime,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = uiState.totalTime,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))

        // Playback Controls
        PlayerControls(
            isPlaying = uiState.isPlaying,
            onPlayPause = onPlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            shuffleEnabled = uiState.shuffleModeEnabled,
            repeatMode = uiState.repeatMode,
            onShuffleToggle = onShuffleToggle,
            onRepeatToggle = onRepeatToggle,
            isFavorite = uiState.isFavorite,
            onFavoriteToggle = onFavoriteToggle,
            onQueueClick = onQueueClick,
            onLyricsClick = onLyricsClick,
            showLyrics = showLyrics
        )
    }
}

/**
 * Options bottom sheet with various actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionsBottomSheet(
    onDismiss: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onEqualizer: () -> Unit,
    onSleepTimer: () -> Unit,
    onEditTags: () -> Unit,
    onEditLyrics: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                onClick = onAddToPlaylist
            )

            OptionItem(
                icon = Icons.Rounded.Album,
                text = "Go to Album",
                onClick = onGoToAlbum
            )

            OptionItem(
                icon = Icons.Rounded.GraphicEq,
                text = "Equalizer",
                onClick = onEqualizer
            )

            OptionItem(
                icon = Icons.Rounded.Timer,
                text = "Sleep Timer",
                onClick = onSleepTimer
            )

            OptionItem(
                icon = Icons.Rounded.Edit,
                text = "Edit Tags",
                onClick = onEditTags
            )

            OptionItem(
                icon = Icons.Rounded.Description,
                text = "Edit Lyrics",
                onClick = onEditLyrics
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GlassmorphicBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val primaryColor = colors.getOrElse(0) { MaterialTheme.colorScheme.primary }
    val secondaryColor = colors.getOrElse(1) { MaterialTheme.colorScheme.secondary }
    val tertiaryColor = colors.getOrElse(2) { MaterialTheme.colorScheme.tertiary }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.3f),
                    radius = size.width * 0.9f
                ),
                center = Offset(size.width * 0.2f, size.height * 0.3f),
                radius = size.width * 0.9f
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.7f),
                    radius = size.width * 0.8f
                ),
                center = Offset(size.width * 0.8f, size.height * 0.7f),
                radius = size.width * 0.8f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tertiaryColor.copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = size.width * 0.7f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                radius = size.width * 0.7f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )
    }
}