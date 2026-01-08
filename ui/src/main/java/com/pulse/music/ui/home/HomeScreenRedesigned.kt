package com.pulse.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.music.core.designsystem.PulseSpacing
import com.pulse.music.core.designsystem.component.PulseEmptyState
import com.pulse.music.core.designsystem.component.PulseSongListItem
import com.pulse.music.domain.model.Song
import kotlinx.coroutines.launch

/**
 * 簡化版首頁 - 左上角選單包含各功能入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenRedesigned(
    viewModel: HomeViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onAlbumsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onDiscoverClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onFoldersClick: () -> Unit = {},
    onDrivingModeClick: () -> Unit = {},
    onPlaybackSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val recoverableAction by viewModel.recoverableAction.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 選單狀態
    var showMenu by remember { mutableStateOf(false) }

    // Launcher for Android 10+ deletion permission
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.handleRecoverableAction(result.resultCode)
    }

    LaunchedEffect(recoverableAction) {
        recoverableAction?.let { sender ->
            val intentSenderRequest = IntentSenderRequest.Builder(sender).build()
            intentSenderLauncher.launch(intentSenderRequest)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scanMusic()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.scanMusic()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.exitSelectionMode()
    }

    // State for Sleep Timer
    var showSleepTimer by remember { mutableStateOf(false) }

    if (showSleepTimer) {
        com.pulse.music.ui.timer.SleepTimerBottomSheet(
            onDismiss = { showSleepTimer = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    // statusBarsPadding removed because MainScreen already handles it
                    .padding(horizontal = 4.dp), // removed vertical padding for compactness
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Menu Button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "選單"
                        )
                    }
                    // 下拉選單
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("睡眠定時器") },
                            leadingIcon = { Icon(Icons.Rounded.Timer, null) },
                            onClick = {
                                showMenu = false
                                showSleepTimer = true
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("專輯") },
                            leadingIcon = { Icon(Icons.Rounded.Album, null) },
                            onClick = {
                                showMenu = false
                                onAlbumsClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("播放清單") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, null) },
                            onClick = {
                                showMenu = false
                                onPlaylistClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("最愛") },
                            leadingIcon = { Icon(Icons.Rounded.Favorite, null) },
                            onClick = {
                                showMenu = false
                                onFavoritesClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("資料夾") },
                            leadingIcon = { Icon(Icons.Rounded.Folder, null) },
                            onClick = {
                                showMenu = false
                                onFoldersClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("探索") },
                            leadingIcon = { Icon(Icons.Rounded.Explore, null) },
                            onClick = {
                                showMenu = false
                                onDiscoverClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("聆聽統計") },
                            leadingIcon = { Icon(Icons.Rounded.BarChart, null) },
                            onClick = {
                                showMenu = false
                                onStatsClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("駕駛模式") },
                            leadingIcon = { Icon(Icons.Rounded.DirectionsCar, null) },
                            onClick = {
                                showMenu = false
                                onDrivingModeClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("播放設定") },
                            leadingIcon = { Icon(Icons.Rounded.Speed, null) },
                            onClick = {
                                showMenu = false
                                onPlaybackSettingsClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("設定") },
                            leadingIcon = { Icon(Icons.Rounded.Settings, null) },
                            onClick = {
                                showMenu = false
                                onSettingsClick()
                            }
                        )
                    }
                }

                // Title
                Text(
                    text = "Pulse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                // Search Button
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "搜尋"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 歌曲控制列
            SongControlRow(
                songCount = uiState.songs.size,
                currentSortOption = uiState.sortOption,
                onShuffleClick = { viewModel.shuffleAll() },
                onSortOptionSelected = { viewModel.setSortOption(it) },
                onPlayAll = {
                    if (uiState.songs.isNotEmpty()) {
                        viewModel.playSong(uiState.songs.first())
                        onSongClick(uiState.songs.first())
                    }
                }
            )

            // 歌曲列表
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.songs.isEmpty()) {
                PulseEmptyState(
                    icon = Icons.Rounded.MusicNote,
                    title = "沒有歌曲",
                    subtitle = "您的音樂庫中還沒有任何歌曲"
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = PulseSpacing.bottomSafeArea),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = uiState.songs,
                            key = { it.id }
                        ) { song ->
                            val isSelected = uiState.selectedSongIds.contains(song.id)
                            val isCurrentlyPlaying = uiState.currentPlayingSongId == song.id && uiState.isPlaying
                            PulseSongListItem(
                                title = song.title,
                                subtitle = "${song.artist} · ${song.album}",
                                albumArtUri = song.albumArtUri,
                                isPlaying = isCurrentlyPlaying,
                                isFavorite = song.isFavorite,
                                duration = formatDuration(song.duration),
                                showDuration = true,
                                showFavorite = false,
                                isSelected = isSelected,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSongSelection(song.id)
                                    } else {
                                        viewModel.playSong(song)
                                        onSongClick(song)
                                    }
                                },
                                onFavoriteClick = { viewModel.toggleFavorite(song.id) },
                                modifier = Modifier.padding(horizontal = PulseSpacing.xs)
                            )
                        }
                    }

                    // Fast Scroller A-Z
                    FastScrollerRedesigned(
                        listState = listState,
                        songs = uiState.songs,
                        scope = scope
                    )
                }
            }
        }
    }
}

/**
 * 歌曲控制列
 */
@Composable
private fun SongControlRow(
    songCount: Int,
    currentSortOption: SortOption,
    onShuffleClick: () -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onPlayAll: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 歌曲數量
        Text(
            text = "$songCount 首歌曲",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // 排序按鈕
        Box {
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = "排序"
                )
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = when (option) {
                                    SortOption.TITLE -> "標題"
                                    SortOption.ARTIST -> "藝人"
                                    SortOption.ALBUM -> "專輯"
                                    SortOption.DATE_ADDED -> "新增日期"
                                    SortOption.DURATION -> "時長"
                                }
                            )
                        },
                        onClick = {
                            onSortOptionSelected(option)
                            showSortMenu = false
                        },
                        leadingIcon = {
                            if (currentSortOption == option) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }

        // 隨機播放按鈕
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "隨機播放"
            )
        }

        // 播放全部按鈕
        FilledTonalIconButton(
            onClick = onPlayAll,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "播放全部"
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.ROOT, "%d:%02d", minutes, seconds)
}

/**
 * 快速定位器 A-Z
 */
@Composable
private fun FastScrollerRedesigned(
    listState: LazyListState,
    songs: List<Song>,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val sections = remember(songs) {
        val map = mutableMapOf<Char, Int>()
        songs.forEachIndexed { index, song ->
            val firstChar = song.title.firstOrNull()?.uppercaseChar()
            val section = if (firstChar != null && firstChar.isLetter()) firstChar else '#'
            if (!map.containsKey(section)) {
                map[section] = index
            }
        }
        map
    }

    val alphabet = remember { ('A'..'Z').toList() + '#' }

    if (sections.isEmpty()) return

    var isDragging by remember { mutableStateOf(false) }
    var activeChar by remember { mutableStateOf<Char?>(null) }

    fun scrollToSection(char: Char) {
        activeChar = char
        val targetIndex = sections[char] ?: run {
            val nextChar = alphabet.dropWhile { it != char }.firstOrNull { sections.containsKey(it) }
            sections[nextChar]
        }

        if (targetIndex != null) {
            scope.launch {
                listState.scrollToItem(targetIndex)
            }
        }
    }

    fun getCharAtIndex(offsetY: Float, totalHeight: Int): Char? {
        if (totalHeight == 0) return null
        val itemHeight = totalHeight.toFloat() / alphabet.size
        val index = (offsetY / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
        return alphabet.getOrNull(index)
    }

    Box(
        modifier = Modifier
            .width(32.dp)
            .fillMaxHeight()
            .padding(top = 16.dp, bottom = 96.dp) // 增加底部間距確保 Z 和 # 不被 Mini Player 遮擋
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val char = getCharAtIndex(offset.y, size.height)
                        if (char != null) scrollToSection(char)
                    },
                    onDragEnd = {
                        isDragging = false
                        activeChar = null
                    },
                    onDragCancel = {
                        isDragging = false
                        activeChar = null
                    },
                    onVerticalDrag = { change, _ ->
                        val char = getCharAtIndex(change.position.y, size.height)
                        if (char != null && char != activeChar) scrollToSection(char)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val char = getCharAtIndex(offset.y, size.height)
                        if (char != null) {
                            scrollToSection(char)
                            scope.launch {
                                kotlinx.coroutines.delay(200)
                                if (!isDragging) activeChar = null
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .background(
                    if (isDragging) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(12.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            alphabet.forEach { char ->
                val isPresent = sections.containsKey(char)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (activeChar == char) MaterialTheme.colorScheme.primary
                                else if (isPresent) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        fontWeight = if (activeChar == char) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        // Active letter bubble
        androidx.compose.animation.AnimatedVisibility(
            visible = isDragging && activeChar != null,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 50.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeChar?.toString() ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


