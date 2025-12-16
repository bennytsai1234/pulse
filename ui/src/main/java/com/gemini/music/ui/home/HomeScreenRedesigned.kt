package com.gemini.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemini.music.core.designsystem.GeminiCorners
import com.gemini.music.core.designsystem.GeminiSize
import com.gemini.music.core.designsystem.GeminiSpacing
import com.gemini.music.core.designsystem.component.GeminiEmptyState
import com.gemini.music.core.designsystem.component.GeminiSongListItem
import com.gemini.music.domain.model.Song
import com.gemini.music.ui.R
import kotlinx.coroutines.launch

/**
 * 重新設計的首頁 - 使用統一設計系統
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
    onFoldersClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val recoverableAction by viewModel.recoverableAction.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Launcher for Android 10+ deletion permission
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.handleRecoverableAction(result.resultCode)
    }

    LaunchedEffect(recoverableAction) {
        recoverableAction?.let { exception ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val intentSenderRequest = IntentSenderRequest.Builder(exception.userAction.actionIntent.intentSender).build()
                intentSenderLauncher.launch(intentSenderRequest)
            }
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

    BackHandler(enabled = uiState.isSelectionMode || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (uiState.isSelectionMode) {
            viewModel.exitSelectionMode()
        }
    }

    // 使用優化後的側邊欄
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            OptimizedDrawerContent(
                onAlbumsClick = {
                    scope.launch { drawerState.close() }
                    onAlbumsClick()
                },
                onPlaylistClick = {
                    scope.launch { drawerState.close() }
                    onPlaylistClick()
                },
                onFavoritesClick = {
                    scope.launch { drawerState.close() }
                    onFavoritesClick()
                },
                onDiscoverClick = {
                    scope.launch { drawerState.close() }
                    onDiscoverClick()
                },
                onFoldersClick = {
                    scope.launch { drawerState.close() }
                    onFoldersClick()
                },
                onStatsClick = {
                    scope.launch { drawerState.close() }
                    onStatsClick()
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    onSettingsClick()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                OptimizedTopBar(
                    isSelectionMode = uiState.isSelectionMode,
                    selectedCount = uiState.selectedSongIds.size,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSearchClick = onSearchClick,
                    onCloseSelection = { viewModel.exitSelectionMode() },
                    onSelectAll = { viewModel.selectAll() },
                    onAddToPlaylist = { viewModel.addToPlaylistClicked() },
                    onDelete = { viewModel.deleteSelected() },
                    onPlaySelected = { viewModel.playSelected() }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (uiState.showAddToPlaylistDialog) {
                    com.gemini.music.ui.component.AddToPlaylistDialog(
                        playlists = uiState.playlists,
                        onDismiss = { viewModel.dismissAddToPlaylistDialog() },
                        onPlaylistSelected = { playlist -> viewModel.addSelectedToPlaylist(playlist) },
                        onCreateNewPlaylist = { name -> viewModel.createPlaylist(name) }
                    )
                }

                if (uiState.songs.isEmpty() && !uiState.isLoading) {
                    GeminiEmptyState(
                        icon = Icons.Rounded.MusicNote,
                        title = stringResource(R.string.no_songs),
                        subtitle = stringResource(R.string.no_songs_message)
                    )
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = GeminiSpacing.bottomSafeArea),
                            modifier = Modifier.weight(1f)
                        ) {
                            // 1. Dashboard Header
                            item {
                                HomeDashboardHeader(
                                    totalSongs = uiState.songs.size,
                                    recentlyAddedSongs = uiState.recentlyAdded,
                                    onPlaylistClick = onPlaylistClick,
                                    onAlbumsClick = onAlbumsClick,
                                    onFavoritesClick = onFavoritesClick,
                                    onDiscoverClick = onDiscoverClick,
                                    onStatsClick = onStatsClick,
                                    onFoldersClick = onFoldersClick,
                                    onRecentSongClick = { song ->
                                        viewModel.playSong(song)
                                        onSongClick(song)
                                    }
                                )
                            }

                            // 2. 控制列
                            item {
                                OptimizedControlRow(
                                    songCount = uiState.songs.size,
                                    currentSortOption = uiState.sortOption,
                                    isFilteringFavorites = uiState.filterFavorites,
                                    onShuffleClick = { viewModel.shuffleAll() },
                                    onSortOptionSelected = { viewModel.setSortOption(it) },
                                    onSelectModeClick = { viewModel.enterSelectionMode() },
                                    onToggleFavorites = { viewModel.toggleFavoritesFilter() },
                                    onPlayAll = {
                                        if (uiState.songs.isNotEmpty()) {
                                            viewModel.playSong(uiState.songs.first())
                                            onSongClick(uiState.songs.first())
                                        }
                                    }
                                )
                            }

                            // 3. 歌曲列表 - 使用統一組件
                            items(
                                items = uiState.songs,
                                key = { it.id }
                            ) { song ->
                                val isSelected = uiState.selectedSongIds.contains(song.id)
                                val isCurrentlyPlaying = uiState.currentPlayingSongId == song.id && uiState.isPlaying
                                GeminiSongListItem(
                                    title = song.title,
                                    subtitle = "${song.artist} · ${song.album}",
                                    albumArtUri = song.albumArtUri,
                                    isPlaying = isCurrentlyPlaying,
                                    isFavorite = song.isFavorite,
                                    duration = formatDuration(song.duration),
                                    showDuration = true,
                                    showFavorite = true,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.toggleSongSelection(song.id)
                                        } else {
                                            viewModel.playSong(song)
                                            onSongClick(song)
                                        }
                                    },
                                    onFavoriteClick = {
                                        viewModel.toggleFavorite(song.id)
                                    },
                                    modifier = Modifier.padding(horizontal = GeminiSpacing.xs)
                                )
                            }
                        }

                        // FastScroller
                        val headerCount = 2 // Dashboard, Controls
                        OptimizedFastScroller(
                            listState = listState,
                            songs = uiState.songs,
                            headerOffset = headerCount,
                            modifier = Modifier.padding(bottom = 80.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 優化的側邊欄
 */
@Composable
private fun OptimizedDrawerContent(
    onAlbumsClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onFoldersClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Spacer(Modifier.height(GeminiSpacing.lg))
        
        // App Logo/Title
        Row(
            modifier = Modifier.padding(GeminiSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(GeminiCorners.md),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(GeminiSpacing.md))
            Column {
                Text(
                    "Gemini Music",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "v1.2.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = GeminiSpacing.sm))
        
        // 音樂庫
        Text(
            "音樂庫",
            modifier = Modifier.padding(
                horizontal = GeminiSpacing.lg,
                vertical = GeminiSpacing.sm
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        
        DrawerItem(Icons.Rounded.Album, "專輯", onAlbumsClick)
        DrawerItem(Icons.AutoMirrored.Rounded.PlaylistAdd, "播放清單", onPlaylistClick)
        DrawerItem(Icons.Rounded.Favorite, "最愛", onFavoritesClick)
        
        Spacer(Modifier.height(GeminiSpacing.md))
        
        // 探索
        Text(
            "探索",
            modifier = Modifier.padding(
                horizontal = GeminiSpacing.lg,
                vertical = GeminiSpacing.sm
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        
        DrawerItem(Icons.Rounded.Explore, "為你推薦", onDiscoverClick)
        DrawerItem(Icons.Rounded.Folder, "資料夾", onFoldersClick)
        DrawerItem(Icons.Rounded.BarChart, "聆聽統計", onStatsClick)
        
        Spacer(Modifier.weight(1f))
        
        HorizontalDivider()
        
        DrawerItem(Icons.Rounded.Settings, "設定", onSettingsClick)
        
        Spacer(Modifier.height(GeminiSpacing.lg))
    }
}

@Composable
private fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, null) },
        modifier = Modifier.padding(horizontal = GeminiSpacing.sm)
    )
}

/**
 * 優化的頂部欄
 */
@Composable
private fun OptimizedTopBar(
    isSelectionMode: Boolean,
    selectedCount: Int,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCloseSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    onPlaySelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(GeminiSize.topBarHeight),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GeminiSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                IconButton(onClick = onCloseSelection) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
                Text(
                    text = "$selectedCount 已選擇",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onPlaySelected) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play")
                }
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Rounded.SelectAll, contentDescription = "Select All")
                }
                IconButton(onClick = onAddToPlaylist) {
                    Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = "Add")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                }
            } else {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search")
                }
            }
        }
    }
}

/**
 * 優化的控制列
 */
@Composable
private fun OptimizedControlRow(
    songCount: Int,
    currentSortOption: SortOption,
    isFilteringFavorites: Boolean,
    onShuffleClick: () -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onSelectModeClick: () -> Unit,
    onToggleFavorites: () -> Unit,
    onPlayAll: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = GeminiSpacing.screenPaddingHorizontal,
                vertical = GeminiSpacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左側: 播放按鈕和數量
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GeminiSpacing.sm)
        ) {
            // 播放全部按鈕
            FilledIconButton(
                onClick = onPlayAll,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "Play All",
                    modifier = Modifier.size(GeminiSize.iconMd)
                )
            }
            
            // 隨機播放按鈕
            FilledTonalIconButton(
                onClick = onShuffleClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(GeminiSize.iconSm)
                )
            }
            
            Text(
                text = "$songCount 首",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 右側: 篩選和排序
        Row(
            horizontalArrangement = Arrangement.spacedBy(GeminiSpacing.xs)
        ) {
            // 最愛篩選
            IconButton(onClick = onToggleFavorites) {
                Icon(
                    imageVector = if (isFilteringFavorites) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Filter Favorites",
                    tint = if (isFilteringFavorites) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 排序選單
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (option) {
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
                                if (option == currentSortOption) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // 選擇模式
            IconButton(onClick = onSelectModeClick) {
                Icon(Icons.Rounded.Checklist, contentDescription = "Select")
            }
        }
    }
}

/**
 * 優化的快速滾動器
 */
@Composable
private fun OptimizedFastScroller(
    listState: LazyListState,
    songs: List<Song>,
    headerOffset: Int = 0,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

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
                listState.scrollToItem(targetIndex + headerOffset)
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
        modifier = modifier
            .width(40.dp)
            .fillMaxHeight()
            .padding(vertical = 32.dp)
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
                    else Color.Transparent,
                    RoundedCornerShape(GeminiCorners.md)
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
                        fontSize = 10.sp,
                        color = when {
                            activeChar == char -> MaterialTheme.colorScheme.primary
                            isPresent -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        },
                        fontWeight = if (activeChar == char) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        // 浮動字母指示器
        AnimatedVisibility(
            visible = isDragging && activeChar != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 50.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeChar?.toString() ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 格式化時長
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
