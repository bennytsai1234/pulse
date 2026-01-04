package com.pulse.music.ui.settings.crossfade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeOff
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.core.designsystem.component.PulseTopBarWithBack
import com.pulse.music.domain.model.CrossfadeCurve

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossfadeSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: CrossfadeSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            PulseTopBarWithBack(
                title = "交叉淡入淡出",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Toggle Section
            item {
                MainToggleCard(
                    enabled = uiState.enabled,
                    onToggle = {
                        viewModel.onEvent(CrossfadeSettingsUiEvent.SetEnabled(it))
                    }
                )
            }

            // Duration Section
            item {
                AnimatedVisibility(
                    visible = uiState.enabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    DurationSection(
                        durationSeconds = uiState.durationSeconds,
                        onDurationChange = {
                            viewModel.onEvent(CrossfadeSettingsUiEvent.SetDuration(it))
                        }
                    )
                }
            }

            // Curve Section
            item {
                AnimatedVisibility(
                    visible = uiState.enabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    CurveSection(
                        selectedCurve = uiState.curve,
                        onCurveChange = {
                            viewModel.onEvent(CrossfadeSettingsUiEvent.SetCurve(it))
                        }
                    )
                }
            }

            // Advanced Settings Section
            item {
                AnimatedVisibility(
                    visible = uiState.enabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    AdvancedSettingsSection(
                        expanded = uiState.showAdvancedSettings,
                        onExpandToggle = {
                            viewModel.onEvent(CrossfadeSettingsUiEvent.ToggleAdvancedSettings)
                        },
                        applyOnManualSkip = uiState.applyOnManualSkip,
                        onApplyOnManualSkipChange = {
                            viewModel.onEvent(CrossfadeSettingsUiEvent.SetApplyOnManualSkip(it))
                        },
                        albumContinuous = uiState.albumContinuous,
                        onAlbumContinuousChange = {
                            viewModel.onEvent(CrossfadeSettingsUiEvent.SetAlbumContinuous(it))
                        },
                        silenceDetection = uiState.silenceDetection,
                        onSilenceDetectionChange = {
                            viewModel.onEvent(CrossfadeSettingsUiEvent.SetSilenceDetection(it))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainToggleCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = if (enabled)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "交叉淡入淡出",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "歌曲之間平滑過渡",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun DurationSection(
    durationSeconds: Int,
    onDurationChange: (Int) -> Unit
) {
    SettingsCard(
        title = "淡入淡出時長",
        icon = Icons.Rounded.Timer
    ) {
        Text(
            text = "${durationSeconds} 秒",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = durationSeconds.toFloat(),
            onValueChange = { onDurationChange(it.toInt()) },
            valueRange = 1f..12f,
            steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CrossfadeSettingsUiState.DURATION_OPTIONS) { duration ->
                FilterChip(
                    selected = durationSeconds == duration,
                    onClick = { onDurationChange(duration) },
                    label = { Text("${duration}s") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun CurveSection(
    selectedCurve: CrossfadeCurve,
    onCurveChange: (CrossfadeCurve) -> Unit
) {
    SettingsCard(
        title = "淡入淡出曲線",
        icon = Icons.Rounded.AutoGraph
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CrossfadeCurve.entries.forEach { curve ->
                CurveOption(
                    curve = curve,
                    selected = selectedCurve == curve,
                    onClick = { onCurveChange(curve) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CurveOption(
    curve: CrossfadeCurve,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (name, description) = when (curve) {
        CrossfadeCurve.LINEAR -> "線性" to "均勻過渡"
        CrossfadeCurve.EXPONENTIAL -> "指數" to "漸進過渡"
        CrossfadeCurve.S_CURVE -> "S 曲線" to "平滑過渡"
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdvancedSettingsSection(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    applyOnManualSkip: Boolean,
    onApplyOnManualSkipChange: (Boolean) -> Unit,
    albumContinuous: Boolean,
    onAlbumContinuousChange: (Boolean) -> Unit,
    silenceDetection: Boolean,
    onSilenceDetectionChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "進階設定",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "收合" else "展開",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    ToggleOption(
                        icon = Icons.Rounded.SkipNext,
                        title = "手動跳轉時套用",
                        subtitle = "切換歌曲時也使用淡入淡出",
                        checked = applyOnManualSkip,
                        onCheckedChange = onApplyOnManualSkipChange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ToggleOption(
                        icon = Icons.Rounded.Album,
                        title = "專輯連續模式",
                        subtitle = "同專輯歌曲使用無縫過渡",
                        checked = albumContinuous,
                        onCheckedChange = onAlbumContinuousChange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ToggleOption(
                        icon = Icons.Rounded.VolumeOff,
                        title = "智慧靜音偵測",
                        subtitle = "自動偵測歌曲結尾靜音段落",
                        checked = silenceDetection,
                        onCheckedChange = onSilenceDetectionChange
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
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
