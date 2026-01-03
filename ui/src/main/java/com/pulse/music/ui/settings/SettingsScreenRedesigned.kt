package com.pulse.music.ui.settings

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.core.designsystem.PulseSpacing
import com.pulse.music.core.designsystem.component.*
import com.pulse.music.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenRedesigned(
    onBackClick: () -> Unit,
    onInternalEqualizerClick: () -> Unit,
    onThemeClick: () -> Unit = {},
    onDrivingModeClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)
            viewModel.addIncludedFolder(it.toString())
        }
    }

    var showSleepTimerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PulseTopBarWithBack(
                title = stringResource(R.string.settings),
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // ===== 外觀設定 =====
            PulseSectionHeader(title = "外觀")

            PulseSettingsItem(
                title = "主題設定",
                subtitle = "調色盤、暗色模式、AMOLED 黑",
                leadingIcon = Icons.Rounded.Palette,
                onClick = onThemeClick
            )

            PulseSettingsItem(
                title = stringResource(R.string.language),
                subtitle = "繁體中文",
                leadingIcon = Icons.Rounded.Language,
                trailingContent = {
                    LanguageSelectorCompact()
                }
            )

            Spacer(modifier = Modifier.height(PulseSpacing.sectionSpacing))

            // ===== 播放設定 =====
            PulseSectionHeader(title = "播放")

            PulseSettingsSwitch(
                title = "使用內建等化器",
                subtitle = "使用 5 段等化器取代系統預設",
                checked = uiState.useInternalEqualizer,
                onCheckedChange = viewModel::updateUseInternalEqualizer,
                leadingIcon = Icons.Rounded.Tune
            )

            PulseSettingsItem(
                title = stringResource(R.string.equalizer),
                subtitle = if (uiState.useInternalEqualizer) "內建 5 段等化器" else "系統等化器",
                leadingIcon = Icons.Rounded.GraphicEq,
                onClick = {
                    if (uiState.useInternalEqualizer) {
                        onInternalEqualizerClick()
                    } else {
                        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "找不到系統等化器", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            PulseSettingsItem(
                title = "睡眠定時器",
                subtitle = "設定時間後自動停止播放",
                leadingIcon = Icons.Rounded.Timer,
                onClick = { showSleepTimerDialog = true }
            )

            PulseSettingsSlider(
                title = stringResource(R.string.min_duration, uiState.minAudioDuration / 1000),
                subtitle = stringResource(R.string.min_duration_desc),
                value = uiState.minAudioDuration.toFloat(),
                onValueChange = { viewModel.updateMinAudioDuration(it.toLong()) },
                valueRange = 0f..60000f,
                steps = 11,
                leadingIcon = Icons.Rounded.Audiotrack,
                valueLabel = {
                    Text(
                        text = "${uiState.minAudioDuration / 1000}s",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )

            // 播放設定入口 (更多播放設定)
            PulseSettingsItem(
                title = "更多播放設定",
                subtitle = "交叉淡入淡出、播放速度等",
                leadingIcon = Icons.Rounded.Speed,
                onClick = { /* Navigate to PlaybackSettingsScreen */ }
            )

            Spacer(modifier = Modifier.height(PulseSpacing.sectionSpacing))

            // ===== 駕駛模式 =====
            PulseSectionHeader(title = "駕駛模式")

            PulseSettingsItem(
                title = "駕駛模式設定",
                subtitle = "大按鈕控制、藍牙自動啟動",
                leadingIcon = Icons.Rounded.DirectionsCar,
                onClick = onDrivingModeClick
            )

            Spacer(modifier = Modifier.height(PulseSpacing.sectionSpacing))

            // ===== 帳戶與同步 =====
            PulseSectionHeader(title = "帳戶與同步")

            // Last.fm
            LastFmSectionCompact()

            PulseDivider(startIndent = 72.dp)

            // Cloud Backup
            com.pulse.music.ui.settings.backup.BackupSection()

            Spacer(modifier = Modifier.height(PulseSpacing.sectionSpacing))

            // ===== 資料庫 =====
            PulseSectionHeader(title = "資料庫")

            PulseSettingsItem(
                title = "重新掃描音樂庫",
                subtitle = "重新掃描裝置上的音樂檔案",
                leadingIcon = Icons.Rounded.Refresh,
                onClick = { viewModel.rescanLibrary() }
            )

            PulseSettingsItem(
                title = "管理資料夾",
                subtitle = "選擇要掃描的資料夾",
                leadingIcon = Icons.Rounded.Folder,
                onClick = { folderPickerLauncher.launch(null) }
            )

            Spacer(modifier = Modifier.height(PulseSpacing.sectionSpacing))

            // ===== 關於 =====
            PulseSectionHeader(title = "關於")

            PulseSettingsItem(
                title = "版本",
                subtitle = "v1.2.0",
                leadingIcon = Icons.Rounded.Info,
                trailingContent = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "開發中",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            )

            // 底部安全區域
            Spacer(modifier = Modifier.height(PulseSpacing.bottomSafeArea))
        }
    }

    // Dialogs
    if (showSleepTimerDialog) {
        SleepTimerDialogRedesigned(
            onDismiss = { showSleepTimerDialog = false },
            onSetTimer = { minutes ->
                viewModel.setSleepTimer(minutes)
                showSleepTimerDialog = false
                Toast.makeText(context, "睡眠定時器已設定：$minutes 分鐘", Toast.LENGTH_SHORT).show()
            },
            onCancelTimer = {
                viewModel.cancelSleepTimer()
                showSleepTimerDialog = false
                Toast.makeText(context, "睡眠定時器已取消", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * 緊湊版語言選擇器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelectorCompact() {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf(
        "系統預設" to "",
        "English" to "en",
        "繁體中文" to "zh-TW",
        "简体中文" to "zh-CN",
        "日本語" to "ja"
    )
    var selectedLanguage by remember { mutableStateOf(languages[0]) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        ) {
            Text(selectedLanguage.first)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (label, code) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        selectedLanguage = label to code
                        expanded = false
                        // Apply language change
                        if (code.isNotEmpty()) {
                            val locale = java.util.Locale.forLanguageTag(code)
                            val localeList = androidx.core.os.LocaleListCompat.create(locale)
                            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
                        } else {
                            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                                androidx.core.os.LocaleListCompat.getEmptyLocaleList()
                            )
                        }
                    }
                )
            }
        }
    }
}

/**
 * 緊湊版 Last.fm 區塊
 */
@Composable
private fun LastFmSectionCompact() {
    PulseSettingsItem(
        title = "Last.fm",
        subtitle = "連接帳戶以記錄聆聽歷史",
        leadingIcon = Icons.Rounded.Audiotrack,
        trailingContent = {
            TextButton(onClick = { /* Open Last.fm settings */ }) {
                Text("連接")
            }
        }
    )
}

/**
 * 重新設計的睡眠定時器對話框
 */
@Composable
private fun SleepTimerDialogRedesigned(
    onDismiss: () -> Unit,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    val presets = listOf(15, 30, 45, 60, 90, 120)
    var customMinutes by remember { mutableFloatStateOf(30f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("睡眠定時器") },
        text = {
            Column {
                Text(
                    text = "選擇時間後自動停止播放",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 快捷按鈕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(3).forEach { minutes ->
                        FilterChip(
                            selected = customMinutes.toInt() == minutes,
                            onClick = { customMinutes = minutes.toFloat() },
                            label = { Text("${minutes}m") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.drop(3).forEach { minutes ->
                        FilterChip(
                            selected = customMinutes.toInt() == minutes,
                            onClick = { customMinutes = minutes.toFloat() },
                            label = { Text("${minutes}m") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 滑桿
                Text(
                    text = "自訂時間: ${customMinutes.toInt()} 分鐘",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = customMinutes,
                    onValueChange = { customMinutes = it },
                    valueRange = 5f..180f,
                    steps = 34
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSetTimer(customMinutes.toInt()) }) {
                Text("設定")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onCancelTimer) {
                    Text("取消定時")
                }
                TextButton(onClick = onDismiss) {
                    Text("關閉")
                }
            }
        }
    )
}


