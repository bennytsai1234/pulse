package com.gemini.music.ui.lyrics

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemini.music.domain.model.EditableLyricLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsEditorScreen(
    viewModel: LyricsEditorViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showImportDialog by remember { mutableStateOf(false) }
    var showOffsetDialog by remember { mutableStateOf(false) }
    var showAddLineDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var editingLine by remember { mutableStateOf<EditableLyricLine?>(null) }
    
    // Handle messages
    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("歌詞編輯器") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // Import
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.Upload, contentDescription = "匯入 LRC")
                    }
                    // Export
                    IconButton(onClick = {
                        viewModel.exportToLrc { lrc ->
                            // TODO: Handle export (copy to clipboard or save to file)
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "匯出 LRC")
                    }
                    // Adjust offset
                    IconButton(onClick = { showOffsetDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "調整偏移")
                    }
                    // Save
                    if (uiState.hasUnsavedChanges) {
                        IconButton(onClick = { viewModel.saveLyrics() }) {
                            Icon(Icons.Default.Save, contentDescription = "儲存")
                        }
                    }
                    // More options (embed/extract)
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MusicNote, contentDescription = "更多選項")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("嵌入歌詞到檔案") },
                                onClick = {
                                    viewModel.embedToFile()
                                    showMoreMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.MusicNote, null)
                                },
                                enabled = uiState.lyrics != null
                            )
                            DropdownMenuItem(
                                text = { Text("從檔案提取歌詞") },
                                onClick = {
                                    viewModel.extractFromFile()
                                    showMoreMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.MusicNote, null)
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddLineDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "新增歌詞行")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.lyrics == null || uiState.lyrics?.lines?.isEmpty() == true) {
            EmptyLyricsState(
                onImportClick = { showImportDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Global offset indicator
                item {
                    OffsetIndicator(
                        offset = uiState.globalOffset,
                        onClick = { showOffsetDialog = true }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                itemsIndexed(
                    items = uiState.lyrics?.lines ?: emptyList(),
                    key = { index, _ -> index }
                ) { index, line ->
                    LyricLineItem(
                        line = line,
                        isSelected = uiState.selectedLineIndex == index,
                        onClick = { viewModel.selectLine(index) },
                        onEdit = { editingLine = line },
                        onDelete = { viewModel.deleteLine(index) }
                    )
                }
            }
        }
    }
    
    // Import Dialog
    if (showImportDialog) {
        ImportLrcDialog(
            onDismiss = { showImportDialog = false },
            onImport = { lrcContent ->
                viewModel.importFromLrc(lrcContent)
                showImportDialog = false
            }
        )
    }
    
    // Offset Dialog
    if (showOffsetDialog) {
        OffsetAdjustDialog(
            currentOffset = uiState.globalOffset,
            onDismiss = { showOffsetDialog = false },
            onConfirm = { offset ->
                viewModel.adjustGlobalOffset(offset)
                showOffsetDialog = false
            }
        )
    }
    
    // Add Line Dialog
    if (showAddLineDialog) {
        AddLineDialog(
            onDismiss = { showAddLineDialog = false },
            onAdd = { timestamp, text ->
                val lastIndex = (uiState.lyrics?.lines?.size ?: 1) - 1
                viewModel.insertLine(lastIndex, timestamp, text)
                showAddLineDialog = false
            }
        )
    }
    
    // Edit Line Dialog
    editingLine?.let { line ->
        EditLineDialog(
            line = line,
            onDismiss = { editingLine = null },
            onSave = { newTimestamp, newText ->
                viewModel.adjustLineTimestamp(line.index, newTimestamp)
                viewModel.updateLineText(line.index, newText)
                editingLine = null
            }
        )
    }
}

@Composable
private fun EmptyLyricsState(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "尚無歌詞",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "點擊下方按鈕匯入 LRC 歌詞檔案",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onImportClick) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("匯入歌詞")
        }
    }
}

@Composable
private fun OffsetIndicator(
    offset: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "全局偏移",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${if (offset >= 0) "+" else ""}${offset}ms",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LyricLineItem(
    line: EditableLyricLine,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "backgroundColor"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timestamp
            Text(
                text = formatTimestamp(line.timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Text
            Text(
                text = line.text.ifEmpty { "(空白行)" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                color = if (line.text.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (isSelected) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "編輯",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "刪除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportLrcDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var lrcContent by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("匯入 LRC 歌詞") },
        text = {
            OutlinedTextField(
                value = lrcContent,
                onValueChange = { lrcContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("貼上 LRC 歌詞內容...") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onImport(lrcContent) },
                enabled = lrcContent.isNotBlank()
            ) {
                Text("匯入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun OffsetAdjustDialog(
    currentOffset: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var offset by remember { mutableStateOf(currentOffset.toFloat()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("調整全局偏移") },
        text = {
            Column {
                Text("偏移量: ${offset.toLong()}ms")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = offset,
                    onValueChange = { offset = it },
                    valueRange = -5000f..5000f,
                    steps = 99
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("-5s", style = MaterialTheme.typography.labelSmall)
                    Text("+5s", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(offset.toLong()) }) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AddLineDialog(
    onDismiss: () -> Unit,
    onAdd: (Long, String) -> Unit
) {
    var timestampText by remember { mutableStateOf("") }
    var lineText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增歌詞行") },
        text = {
            Column {
                OutlinedTextField(
                    value = timestampText,
                    onValueChange = { timestampText = it },
                    label = { Text("時間戳 (毫秒)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lineText,
                    onValueChange = { lineText = it },
                    label = { Text("歌詞文字") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timestamp = timestampText.toLongOrNull() ?: 0L
                    onAdd(timestamp, lineText)
                },
                enabled = timestampText.isNotBlank()
            ) {
                Text("新增")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EditLineDialog(
    line: EditableLyricLine,
    onDismiss: () -> Unit,
    onSave: (Long, String) -> Unit
) {
    var timestampText by remember { mutableStateOf(line.timestamp.toString()) }
    var lineText by remember { mutableStateOf(line.text) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("編輯歌詞行") },
        text = {
            Column {
                OutlinedTextField(
                    value = timestampText,
                    onValueChange = { timestampText = it },
                    label = { Text("時間戳 (毫秒)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lineText,
                    onValueChange = { lineText = it },
                    label = { Text("歌詞文字") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timestamp = timestampText.toLongOrNull() ?: line.timestamp
                    onSave(timestamp, lineText)
                }
            ) {
                Text("儲存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun formatTimestamp(ms: Long): String {
    val minutes = (ms / 60000).toInt()
    val seconds = ((ms % 60000) / 1000).toInt()
    val hundredths = ((ms % 1000) / 10).toInt()
    return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
}
