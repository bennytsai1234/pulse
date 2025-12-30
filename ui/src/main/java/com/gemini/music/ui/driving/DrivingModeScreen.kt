package com.gemini.music.ui.driving

import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gemini.music.ui.R

/**
 * 駕駛模式畫面 - 大按鈕、簡化 UI、專為駕駛設計
 */
@Composable
fun DrivingModeScreen(
    viewModel: DrivingModeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    // 處理副作用
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DrivingModeUiEffect.NavigateBack -> onNavigateBack()
                is DrivingModeUiEffect.Vibrate -> {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val manager = context.getSystemService(VibratorManager::class.java)
                        manager?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Vibrator::class.java)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                is DrivingModeUiEffect.Speak -> {
                    Toast.makeText(context, context.getString(R.string.driving_mode_tts_prefix, effect.text), Toast.LENGTH_SHORT).show()
                }
                is DrivingModeUiEffect.LaunchVoiceSearch -> {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    }
                    try {
                        voiceSearchLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.driving_mode_voice_search_unavailable), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                androidx.compose.foundation.gestures.detectHorizontalDragGestures(
                    onDragStart = { offsetX = 0f },
                    onDragEnd = {
                        if (offsetX > 100) {
                            viewModel.onEvent(DrivingModeUiEvent.PreviousTrack)
                        } else if (offsetX < -100) {
                            viewModel.onEvent(DrivingModeUiEvent.NextTrack)
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    }
                )
            }
    ) {
        // 背景模糊專輯封面
        uiState.albumArtUri?.let { uri ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }

        // 半透明疊層
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 頂部狀態列
            DrivingModeHeader(
                connectedDevice = uiState.connectedDevice,
                onExit = { viewModel.onEvent(DrivingModeUiEvent.ExitDrivingMode) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 專輯封面
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uiState.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.driving_mode_album_art_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 歌曲資訊
            Text(
                text = uiState.currentSong?.title ?: stringResource(R.string.driving_mode_not_playing),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = uiState.currentSong?.artist ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 進度條與快進快退
            DrivingModeSeekControls(
                currentPosition = uiState.currentPositionMs,
                duration = uiState.durationMs,
                onSeek = { viewModel.onEvent(DrivingModeUiEvent.SeekTo(it)) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 主控制按鈕 (超大)
            DrivingModeControls(
                isPlaying = uiState.isPlaying,
                onPlayPause = { viewModel.onEvent(DrivingModeUiEvent.PlayPause) },
                onPrevious = { viewModel.onEvent(DrivingModeUiEvent.PreviousTrack) },
                onNext = { viewModel.onEvent(DrivingModeUiEvent.NextTrack) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 附加控制
            DrivingModeSecondaryControls(
                onVolumeUp = { viewModel.onEvent(DrivingModeUiEvent.VolumeUp) },
                onVolumeDown = { viewModel.onEvent(DrivingModeUiEvent.VolumeDown) },
                onQueue = { viewModel.onEvent(DrivingModeUiEvent.ToggleQueue) }
            )
        }

        // 佇列面板
        AnimatedVisibility(
            visible = uiState.showQueue,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            DrivingModeQueue(
                songs = uiState.queueSongs,
                currentSong = uiState.currentSong,
                onSelectSong = { index ->
                    viewModel.onEvent(DrivingModeUiEvent.SelectFromQueue(index))
                },
                onClose = { viewModel.onEvent(DrivingModeUiEvent.ToggleQueue) }
            )
        }
    }
}

@Composable
private fun DrivingModeHeader(
    connectedDevice: String?,
    onExit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 連接狀態
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.BluetoothConnected,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = connectedDevice ?: stringResource(R.string.driving_mode),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }

        // 退出按鈕
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.driving_mode_exit_desc),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun DrivingModeSeekControls(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rewind 10s
            IconButton(
                onClick = { onSeek((currentPosition - 10000).coerceAtLeast(0)) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay10,
                    contentDescription = stringResource(R.string.driving_mode_rewind_10s),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            // Forward 30s
            IconButton(
                onClick = { onSeek((currentPosition + 30000).coerceAtMost(duration)) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Forward30,
                    contentDescription = stringResource(R.string.driving_mode_forward_30s),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DrivingModeControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous - 大按鈕
        DrivingControlButton(
            icon = Icons.Rounded.SkipPrevious,
            size = 80.dp,
            contentDescription = stringResource(R.string.driving_mode_prev_desc),
            onClick = onPrevious
        )

        // Play/Pause - 超大按鈕
        DrivingControlButton(
            icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            size = 120.dp,
            isPrimary = true,
            contentDescription = if (isPlaying) stringResource(R.string.driving_mode_pause_desc) else stringResource(R.string.driving_mode_play_desc),
            onClick = onPlayPause
        )

        // Next - 大按鈕
        DrivingControlButton(
            icon = Icons.Rounded.SkipNext,
            size = 80.dp,
            contentDescription = stringResource(R.string.driving_mode_next_desc),
            onClick = onNext
        )
    }
}

@Composable
private fun DrivingControlButton(
    icon: ImageVector,
    size: androidx.compose.ui.unit.Dp,
    isPrimary: Boolean = false,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = if (isPrimary) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
private fun DrivingModeSecondaryControls(
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onQueue: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DrivingSecondaryButton(
            icon = Icons.AutoMirrored.Rounded.VolumeDown,
            label = stringResource(R.string.driving_mode_volume_down),
            onClick = onVolumeDown
        )

        DrivingSecondaryButton(
            icon = Icons.AutoMirrored.Rounded.QueueMusic,
            label = stringResource(R.string.driving_mode_queue),
            onClick = onQueue
        )

        DrivingSecondaryButton(
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            label = stringResource(R.string.driving_mode_volume_up),
            onClick = onVolumeUp
        )
    }
}

@Composable
private fun DrivingSecondaryButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DrivingModeQueue(
    songs: List<com.gemini.music.domain.model.Song>,
    currentSong: com.gemini.music.domain.model.Song?,
    onSelectSong: (Int) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.driving_mode_queue_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.driving_mode_close_desc),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                itemsIndexed(songs) { index, song ->
                    DrivingQueueItem(
                        song = song,
                        isCurrentPlaying = song.id == currentSong?.id,
                        onClick = { onSelectSong(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrivingQueueItem(
    song: com.gemini.music.domain.model.Song,
    isCurrentPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrentPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCurrentPlaying) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCurrentPlaying) MaterialTheme.colorScheme.primary else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
