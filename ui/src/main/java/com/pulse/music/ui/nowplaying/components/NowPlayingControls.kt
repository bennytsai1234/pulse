package com.pulse.music.ui.nowplaying.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pulse.music.domain.model.RepeatMode

/**
 * Option item for the bottom sheet menu
 */
@Composable
fun OptionItem(
    icon: ImageVector,
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

/**
 * Main playback control buttons (shuffle, previous, play/pause, next, repeat)
 * and secondary actions (favorite, lyrics, queue)
 */
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
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit = {},
    showLyrics: Boolean = false
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

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
                tint = if (shuffleEnabled) primary else onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        // Previous Button
        IconButton(onClick = onSkipPrevious) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = onSurface,
                modifier = Modifier.size(36.dp)
            )
        }

        // Play/Pause Button
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(primary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = onPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        // Next Button
        IconButton(onClick = onSkipNext) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = onSurface,
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
            val tint = if (repeatMode == RepeatMode.OFF) onSurfaceVariant else primary
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
                tint = if (isFavorite) Color.Red else onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        // Lyrics Button
        IconButton(onClick = onLyricsClick) {
            Icon(
                imageVector = Icons.Rounded.Description,
                contentDescription = "Lyrics",
                tint = if (showLyrics) primary else onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        // Queue Button
        IconButton(onClick = onQueueClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                contentDescription = "Queue",
                tint = onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Top bar controls with back button, queue, lyrics, and more options
 */
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


