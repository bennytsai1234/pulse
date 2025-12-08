package com.gemini.music.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.AlarmOff
import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import com.gemini.music.domain.model.ScanStatus
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemini.music.domain.repository.UserPreferencesRepository
import com.gemini.music.core.common.util.StorageUtils
import androidx.compose.ui.res.stringResource
import com.gemini.music.ui.R
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val path = StorageUtils.getPathFromUri(it)
            if (path != null) {
                viewModel.addIncludedFolder(path)
            } else {
                Toast.makeText(context, "Could not resolve folder path", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Theme Mode
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            ListItem(
                headlineContent = { Text(stringResource(R.string.theme_mode)) },
                trailingContent = {
                    ThemeModeSelector(
                        currentMode = uiState.themeMode,
                        onModeSelected = viewModel::updateThemeMode
                    )
                }
            )

            // Language
            ListItem(
                headlineContent = { Text(stringResource(R.string.language)) },
                leadingContent = { Icon(Icons.Rounded.Language, null) },
                trailingContent = {
                    LanguageSelector()
                }
            )


            // Equalizer
            ListItem(
                headlineContent = { Text(stringResource(R.string.equalizer)) },
                leadingContent = { Icon(Icons.Rounded.GraphicEq, null) },
                modifier = Modifier.clickable {
                    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "No System Equalizer found", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            
            // Sleep Timer
            var showSleepTimerDialog by remember { mutableStateOf(false) }
            ListItem(
                headlineContent = { Text("Sleep Timer") },
                supportingContent = { Text("Stop playback after set time") },
                leadingContent = { Icon(Icons.Rounded.Timer, null) },
                modifier = Modifier.clickable { showSleepTimerDialog = true }
            )

            if (showSleepTimerDialog) {
                SleepTimerDialog(
                    onDismiss = { showSleepTimerDialog = false },
                    onSetTimer = { minutes ->
                        viewModel.setSleepTimer(minutes)
                        showSleepTimerDialog = false
                        Toast.makeText(context, "Sleep timer set for $minutes minutes", Toast.LENGTH_SHORT).show()
                    },
                    onCancelTimer = {
                        viewModel.cancelSleepTimer()
                        showSleepTimerDialog = false
                        Toast.makeText(context, "Sleep timer cancelled", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Min Duration Slider
            Text(
                text = stringResource(R.string.min_duration, uiState.minAudioDuration / 1000),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            Slider(
                value = uiState.minAudioDuration.toFloat(),
                onValueChange = { viewModel.updateMinAudioDuration(it.toLong()) },
                valueRange = 0f..60000f, // 0s to 60s
                steps = 11,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = stringResource(R.string.min_duration_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Scan Folders
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.included_folders),
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = { folderPickerLauncher.launch(null) }) {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_folder))
                }
            }
            
            if (uiState.includedFolders.isEmpty()) {
                Text(
                    text = stringResource(R.string.scanning_default),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.includedFolders.toList()) { path ->
                        ListItem(
                            headlineContent = { Text(path) },
                            trailingContent = {
                                IconButton(onClick = { viewModel.removeIncludedFolder(path) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Remove")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.rescanLibrary() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.rescan_library))
            }
        }
    }

    // Scan Progress Dialog
    val scanStatus = uiState.scanStatus
    if (scanStatus !is ScanStatus.Idle) {
        AlertDialog(
            onDismissRequest = {
                if (scanStatus !is ScanStatus.Scanning) {
                    viewModel.resetScanStatus()
                }
            },
            title = {
                Text(
                    text = when (scanStatus) {
                        is ScanStatus.Scanning -> "Scanning Library..."
                        is ScanStatus.Completed -> "Scan Completed"
                        is ScanStatus.Failed -> "Scan Failed"
                        else -> ""
                    }
                )
            },
            text = {
                Column {
                    when (scanStatus) {
                        is ScanStatus.Scanning -> {
                            val progress = scanStatus.progress
                            val total = scanStatus.total
                            val message = scanStatus.currentFile

                            if (total > 0) {
                                LinearProgressIndicator(
                                    progress = { progress.toFloat() / total },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.scanning)) // Simplified for now
                        }
                        is ScanStatus.Completed -> {
                            Text(stringResource(R.string.scan_success_message, scanStatus.totalAdded))
                        }
                        is ScanStatus.Failed -> {
                            Text(stringResource(R.string.scan_error_message, scanStatus.error))
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                if (scanStatus !is ScanStatus.Scanning) {
                    androidx.compose.material3.TextButton(onClick = { viewModel.resetScanStatus() }) {
                        Text("OK")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModeSelector(
    currentMode: String,
    onModeSelected: (String) -> Unit
) {
    val themeModes = listOf(UserPreferencesRepository.THEME_SYSTEM, UserPreferencesRepository.THEME_LIGHT, UserPreferencesRepository.THEME_DARK)
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.width(150.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = currentMode,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false; focusManager.clearFocus() }
            ) {
                themeModes.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode) },
                        onClick = {
                            onModeSelected(mode)
                            expanded = false
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector() {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (!currentLocales.isEmpty) currentLocales[0]?.toLanguageTag() else "en-US"
    
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.width(150.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = if (currentTag?.contains("zh") == true) "繁體中文" else "English",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("English") },
                    onClick = {
                        val appLocale = LocaleListCompat.forLanguageTags("en-US")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("繁體中文") },
                    onClick = {
                        val appLocale = LocaleListCompat.forLanguageTags("zh-TW")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Timer, null) },
        title = { Text("Sleep Timer") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val options = listOf(15, 30, 45, 60, 90, 120)
                options.chunked(3).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowOptions.forEach { minutes ->
                            androidx.compose.material3.OutlinedButton(
                                onClick = { onSetTimer(minutes) }
                            ) {
                                Text("$minutes min")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onCancelTimer,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.AlarmOff, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Turn Off Timer")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
