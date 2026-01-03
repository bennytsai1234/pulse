package com.pulse.music.ui.equalizer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.core.designsystem.component.PulseTopBarWithBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
        audioSessionId: Int,
        onBackClick: () -> Unit,
        viewModel: EqualizerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(audioSessionId) { viewModel.initializeEqualizer(audioSessionId) }

    Scaffold(
            topBar = {
                PulseTopBarWithBack(
                        title = "Equalizer",
                        onBackClick = onBackClick,
                        actions = {
                            IconButton(onClick = { viewModel.resetToFlat() }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Reset")
                            }
                        }
                )
            }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (!uiState.isAvailable) {
                // Error State
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                                text = "Equalizer Not Available",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        uiState.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                // Enable Switch
                Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Enable Equalizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                    )
                    Switch(
                            checked = uiState.isEnabled,
                            onCheckedChange = { viewModel.setEnabled(it) },
                            colors =
                                    SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor =
                                                    MaterialTheme.colorScheme.primaryContainer
                                    )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets
                if (uiState.presets.isNotEmpty()) {
                    Text(
                            text = "Presets",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(uiState.presets) { preset ->
                            FilterChip(
                                    selected = preset.index == uiState.currentPresetIndex,
                                    onClick = { viewModel.selectPreset(preset.index) },
                                    label = { Text(preset.name) },
                                    enabled = uiState.isEnabled,
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

                Spacer(modifier = Modifier.height(32.dp))

                // Equalizer Bands
                Text(
                        text = "Frequency Bands",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    uiState.bands.forEach { band ->
                        EqualizerBandSlider(
                                band = band,
                                enabled = uiState.isEnabled,
                                onLevelChange = { viewModel.setBandLevel(band.index, it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // dB Labels
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                            text = "-${(uiState.bands.firstOrNull()?.maxLevel ?: 1500) / 100}dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = "0dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = "+${(uiState.bands.firstOrNull()?.maxLevel ?: 1500) / 100}dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ==================== Bass Boost ====================
                if (uiState.bassBoostAvailable) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "Bass Boost",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                        )
                        Switch(
                                checked = uiState.bassBoostEnabled,
                                onCheckedChange = { viewModel.setBassBoostEnabled(it) },
                                enabled = uiState.isEnabled,
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor =
                                                        MaterialTheme.colorScheme.primary,
                                                checkedTrackColor =
                                                        MaterialTheme.colorScheme.primaryContainer
                                        )
                        )
                    }

                    if (uiState.bassBoostEnabled && uiState.isEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    text = "Strength",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(80.dp)
                            )
                            Slider(
                                    value = uiState.bassBoostStrength / 1000f,
                                    onValueChange = {
                                        viewModel.setBassBoostStrength((it * 1000).toInt())
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors =
                                            SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor =
                                                            MaterialTheme.colorScheme.primary
                                            )
                            )
                            Text(
                                    text = "${(uiState.bassBoostStrength / 10)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(48.dp),
                                    textAlign = TextAlign.End
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ==================== Virtualizer ====================
                if (uiState.virtualizerAvailable) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "Virtualizer (Surround)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                        )
                        Switch(
                                checked = uiState.virtualizerEnabled,
                                onCheckedChange = { viewModel.setVirtualizerEnabled(it) },
                                enabled = uiState.isEnabled,
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor =
                                                        MaterialTheme.colorScheme.primary,
                                                checkedTrackColor =
                                                        MaterialTheme.colorScheme.primaryContainer
                                        )
                        )
                    }

                    if (uiState.virtualizerEnabled && uiState.isEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    text = "Strength",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(80.dp)
                            )
                            Slider(
                                    value = uiState.virtualizerStrength / 1000f,
                                    onValueChange = {
                                        viewModel.setVirtualizerStrength((it * 1000).toInt())
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors =
                                            SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor =
                                                            MaterialTheme.colorScheme.primary
                                            )
                            )
                            Text(
                                    text = "${(uiState.virtualizerStrength / 10)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(48.dp),
                                    textAlign = TextAlign.End
                            )
                        }
                    }
                }

                // ==================== Loudness Enhancer ====================
                if (uiState.loudnessAvailable) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "Loudness Enhancer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                        )
                        Switch(
                                checked = uiState.loudnessEnabled,
                                onCheckedChange = { viewModel.setLoudnessEnabled(it) },
                                enabled = uiState.isEnabled,
                                colors =
                                        SwitchDefaults.colors(
                                                checkedThumbColor =
                                                        MaterialTheme.colorScheme.primary,
                                                checkedTrackColor =
                                                        MaterialTheme.colorScheme.primaryContainer
                                        )
                        )
                    }

                    if (uiState.loudnessEnabled && uiState.isEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                    Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                    value = uiState.loudnessGain / 1000f,
                                    onValueChange = {
                                        viewModel.setLoudnessGain((it * 1000).toInt())
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors =
                                            SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor =
                                                            MaterialTheme.colorScheme.primary
                                            )
                            )
                            Text(
                                    text = "+${uiState.loudnessGain / 100}dB",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(48.dp),
                                    textAlign = TextAlign.End
                            )
                        }
                    }
                }

                // ==================== Custom Presets Section ====================
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "My Presets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                    )
                    IconButton(
                            onClick = { viewModel.showSavePresetDialog() },
                            enabled = uiState.isEnabled
                    ) {
                        Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Save as Preset",
                                tint =
                                        if (uiState.isEnabled) MaterialTheme.colorScheme.primary
                                        else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.3f
                                                )
                        )
                    }
                }

                if (uiState.customPresets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.customPresets) { preset ->
                            FilterChip(
                                    selected = preset.id == uiState.currentCustomPresetId,
                                    onClick = { viewModel.applyCustomPreset(preset) },
                                    label = { Text(preset.name) },
                                    enabled = uiState.isEnabled,
                                    trailingIcon = {
                                        Icon(
                                                Icons.Rounded.Delete,
                                                contentDescription = "Delete",
                                                modifier =
                                                        Modifier.size(16.dp).clickable {
                                                            viewModel.deleteCustomPreset(preset)
                                                        }
                                        )
                                    },
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
                } else {
                    Text(
                            text = "No custom presets saved yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Save Preset Dialog
    if (uiState.showSavePresetDialog) {
        AlertDialog(
                onDismissRequest = { viewModel.hideSavePresetDialog() },
                title = { Text("Save Preset") },
                text = {
                    Column {
                        Text("Enter a name for this preset:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                                value = uiState.presetNameInput,
                                onValueChange = { viewModel.updatePresetNameInput(it) },
                                label = { Text("Preset Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                            onClick = { viewModel.saveCurrentAsPreset() },
                            enabled = uiState.presetNameInput.isNotBlank()
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideSavePresetDialog() }) { Text("Cancel") }
                }
        )
    }
}

@Composable
fun EqualizerBandSlider(band: EqualizerBand, enabled: Boolean, onLevelChange: (Float) -> Unit) {
    val activeColor by
            animateColorAsState(
                    targetValue =
                            if (enabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    animationSpec =
                            tween(150, easing = androidx.compose.animation.core.LinearEasing),
                    label = "ActiveColor"
            )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
        // Custom Vertical Slider using rotated Slider
        Box(modifier = Modifier.weight(1f).width(48.dp), contentAlignment = Alignment.Center) {
            // Background Track
            Box(
                    modifier =
                            Modifier.width(8.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Active Track
            Box(
                    modifier =
                            Modifier.width(8.dp)
                                    .fillMaxHeight(fraction = band.normalizedLevel)
                                    .align(Alignment.BottomCenter)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    activeColor,
                                                                    activeColor.copy(alpha = 0.5f)
                                                            )
                                            )
                                    )
            )

            // Slider (invisible, for interaction)
            Slider(
                    value = band.normalizedLevel,
                    onValueChange = onLevelChange,
                    enabled = enabled,
                    modifier =
                            Modifier.fillMaxHeight()
                                    .graphicsLayer {
                                        rotationZ = 270f // Rotate to make vertical
                                    }
                                    .width(200.dp), // This becomes height after rotation
                    colors =
                            SliderDefaults.colors(
                                    thumbColor = Color.Transparent,
                                    activeTrackColor = Color.Transparent,
                                    inactiveTrackColor = Color.Transparent
                            )
            )

            // Level Indicator (Thumb)
            Box(
                    modifier =
                            Modifier.align(Alignment.BottomCenter)
                                    .padding(bottom = (band.normalizedLevel * 180).dp)
                                    .width(24.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                            if (enabled) activeColor
                                            else
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.5f
                                                    )
                                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Frequency Label
        Text(
                text = band.label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


