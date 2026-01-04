package com.pulse.music.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.core.designsystem.component.PulseTopBarWithBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsScreen(
        onBackClick: () -> Unit,
        onCrossfadeClick: () -> Unit = {},
        viewModel: PlaybackSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            topBar = {
                PulseTopBarWithBack(title = "Playback Settings", onBackClick = onBackClick)
            }
    ) { padding ->
        LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Playback Speed Section
            item {
                SettingsSection(title = "Playback Speed", icon = Icons.Rounded.Speed) {
                    Text(
                            text = "${String.format(java.util.Locale.ROOT, "%.2f", uiState.playbackSpeed)}x",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                            value = uiState.playbackSpeed,
                            onValueChange = { viewModel.setPlaybackSpeed(it) },
                            valueRange = 0.5f..2.0f,
                            steps = 14,
                            colors =
                                    SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PlaybackSettingsViewModel.SPEED_PRESETS) { speed ->
                            FilterChip(
                                    selected = uiState.playbackSpeed == speed,
                                    onClick = { viewModel.setPlaybackSpeed(speed) },
                                    label = { Text("${speed}x") },
                                    colors =
                                            FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor =
                                                            MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor =
                                                            MaterialTheme.colorScheme.onPrimary
                                            )
                            )
                        }
                    }
                }
            }

            // Crossfade Section
            item {
                SettingsSection(
                    title = "Crossfade",
                    icon = Icons.Rounded.Tune,
                    onClick = onCrossfadeClick
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                    text =
                                            if (uiState.crossfadeDuration == 0) "Off"
                                            else "${uiState.crossfadeDuration}s",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                    text = "Smooth transition between tracks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Open crossfade settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sleep Timer Fade Section
            item {
                SettingsSection(title = "Sleep Timer", icon = Icons.Rounded.Timer) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                    text = "Fade Out",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                            )
                            Text(
                                    text = "Gradually lower volume before stopping",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                                checked = uiState.sleepTimerFadeOut,
                                onCheckedChange = { viewModel.setSleepTimerFadeOut(it) },
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor =
                                                        MaterialTheme.colorScheme.primary,
                                                checkedTrackColor =
                                                        MaterialTheme.colorScheme.primaryContainer
                                        )
                        )
                    }

                    if (uiState.sleepTimerFadeOut) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Fade Duration", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PlaybackSettingsViewModel.FADE_DURATION_OPTIONS) { duration ->
                                FilterChip(
                                        selected = uiState.sleepTimerFadeDuration == duration,
                                        onClick = { viewModel.setSleepTimerFadeDuration(duration) },
                                        label = { Text("${duration}s") },
                                        colors =
                                                FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor =
                                                                MaterialTheme.colorScheme.tertiary,
                                                        selectedLabelColor =
                                                                MaterialTheme.colorScheme.onTertiary
                                                )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = RoundedCornerShape(16.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}


