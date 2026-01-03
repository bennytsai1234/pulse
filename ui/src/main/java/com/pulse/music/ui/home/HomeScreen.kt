package com.pulse.music.ui.home

import java.util.Calendar
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pulse.music.domain.model.Song
import com.pulse.music.core.designsystem.component.PulseEmptyState
import com.pulse.music.core.designsystem.component.PulseTopBar
import com.pulse.music.ui.component.SongListItem
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
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

    androidx.compose.runtime.LaunchedEffect(recoverableAction) {
        recoverableAction?.let { sender ->
             val intentSenderRequest = IntentSenderRequest.Builder(sender).build()
             intentSenderLauncher.launch(intentSenderRequest)
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.scanMusic()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(com.pulse.music.ui.R.string.library),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    style = MaterialTheme.typography.titleSmall
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(com.pulse.music.ui.R.string.albums)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onAlbumsClick()
                    },
                    icon = { Icon(Icons.Rounded.Folder, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(com.pulse.music.ui.R.string.playlists)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onPlaylistClick()
                    },
                    icon = { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Favorites") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onFavoritesClick()
                    },
                    icon = { Icon(Icons.Rounded.Favorite, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    "探索",
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    style = MaterialTheme.typography.titleSmall
                )
                NavigationDrawerItem(
                    label = { Text("為你推薦") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onDiscoverClick()
                    },
                    icon = { Icon(Icons.Rounded.Explore, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("資料夾") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onFoldersClick()
                    },
                    icon = { Icon(Icons.Rounded.Folder, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("聆聽統計") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onStatsClick()
                    },
                    icon = { Icon(Icons.Rounded.Analytics, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.height(24.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(com.pulse.music.ui.R.string.settings)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSettingsClick()
                    },
                    icon = { Icon(Icons.Rounded.Settings, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
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
                    com.pulse.music.ui.component.AddToPlaylistDialog(
                        playlists = uiState.playlists,
                        onDismiss = { viewModel.dismissAddToPlaylistDialog() },
                        onPlaylistSelected = { playlist -> viewModel.addSelectedToPlaylist(playlist) },
                        onCreateNewPlaylist = { name -> viewModel.createPlaylist(name) }
                    )
                }

                if (uiState.songs.isEmpty() && !uiState.isLoading) {
                    PulseEmptyState(
                        icon = Icons.Rounded.Album,
                        title = stringResource(com.pulse.music.ui.R.string.no_songs),
                        subtitle = stringResource(com.pulse.music.ui.R.string.no_songs_message),
                        action = {
                            Button(onClick = { viewModel.scanMusic() }) {
                                Text("Rescan")
                            }
                        }
                    )
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = 100.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // 1. Dashboard Greeting
                            item {
                                GreetingHeader()
                            }

                            // 2. Recently Added Carousel (Dashboard Style)
                            if (uiState.recentlyAdded.isNotEmpty()) {
                                item {
                                    RecentlyAddedRow(
                                        songs = uiState.recentlyAdded.take(15),
                                        onSongClick = {
                                            viewModel.playSong(it)
                                            onSongClick(it)
                                        }
                                    )
                                }
                            }

                            // 3. Controls & Song List Header
                            item {
                                ControlRow(
                                    songCount = uiState.songs.size,
                                    currentSortOption = uiState.sortOption,
                                    isFilteringFavorites = uiState.filterFavorites,
                                    onShuffleClick = { viewModel.shuffleAll() },
                                    onSortOptionSelected = { viewModel.setSortOption(it) },
                                    onSelectModeClick = { viewModel.enterSelectionMode() },
                                    onToggleFavorites = { viewModel.toggleFavoritesFilter() }
                                )
                            }

                            // 4. All Songs List (Filtered by VM)
                            items(
                                items = uiState.songs,
                                key = { it.id }
                            ) { song ->
                                val isSelected = uiState.selectedSongIds.contains(song.id)
                                SongListItem(
                                    song = song,
                                    isSelected = isSelected,
                                    isSelectionMode = uiState.isSelectionMode,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.toggleSongSelection(song.id)
                                        } else {
                                            viewModel.playSong(song)
                                            onSongClick(song)
                                        }
                                    },
                                    onLongClick = {
                                        if (!uiState.isSelectionMode) {
                                            viewModel.enterSelectionMode()
                                            viewModel.toggleSongSelection(song.id)
                                        }
                                    }
                                )
                            }
                        }

                        // FastScroller handles headers offset
                        val headerCount = 3 // Greeting, Recent, Controls
                        FastScroller(
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

@Composable
fun GreetingHeader() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    // Subtle background gradient based on time
    val gradientColors = when (hour) {
        in 5..11 -> listOf(Color(0xFFFFA726).copy(alpha = 0.15f), Color.Transparent) // Orange
        in 12..17 -> listOf(Color(0xFF29B6F6).copy(alpha = 0.15f), Color.Transparent) // Blue
        else -> listOf(Color(0xFF7E57C2).copy(alpha = 0.15f), Color.Transparent) // Purple
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun HomeTopBar(
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
    if (isSelectionMode) {
        // Contextual Action Bar appearance for Selection Mode
        PulseTopBar(
            title = {
                Text(
                    text = pluralStringResource(com.pulse.music.ui.R.plurals.selected_count, selectedCount, selectedCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onCloseSelection) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            },
            actions = {
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
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant, // Distinction for selection mode
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    } else {
        // Standard Home AppBar
        PulseTopBar(
            title = { /* Empty or App Name if desired, keeping empty as per original functionality */ },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search")
                }
            }
        )
    }
}

@Composable
fun ControlRow(
    songCount: Int,
    currentSortOption: SortOption,
    isFilteringFavorites: Boolean,
    onShuffleClick: () -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onSelectModeClick: () -> Unit,
    onToggleFavorites: () -> Unit
) {
    var showSortMenu by remember { androidx.compose.runtime.mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onShuffleClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(com.pulse.music.ui.R.string.shuffle), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "$songCount " + stringResource(com.pulse.music.ui.R.string.songs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row {
            // Favorites Toggle
            IconButton(onClick = onToggleFavorites) {
                Icon(
                    imageVector = if (isFilteringFavorites) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Filter Favorites",
                    tint = if (isFilteringFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOption.values().forEach { option ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                val label = when(option) {
                                    SortOption.TITLE -> stringResource(com.pulse.music.ui.R.string.sort_title)
                                    SortOption.ARTIST -> stringResource(com.pulse.music.ui.R.string.sort_artist)
                                    SortOption.ALBUM -> stringResource(com.pulse.music.ui.R.string.sort_album)
                                    SortOption.DATE_ADDED -> stringResource(com.pulse.music.ui.R.string.sort_date_added)
                                    SortOption.DURATION -> stringResource(com.pulse.music.ui.R.string.sort_duration)
                                }
                                Text(label)
                            },
                            onClick = {
                                onSortOptionSelected(option)
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (option == currentSortOption) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            }

            IconButton(onClick = onSelectModeClick) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Select")
            }
        }
    }
}

@Composable
fun FastScroller(
    listState: LazyListState,
    songs: List<Song>,
    modifier: Modifier = Modifier,
    headerOffset: Int = 0
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

    var isDragging by remember { androidx.compose.runtime.mutableStateOf(false) }
    var activeChar by remember { androidx.compose.runtime.mutableStateOf<Char?>(null) }

    fun scrollToSection(char: Char) {
        activeChar = char
        val targetIndex = sections[char] ?: run {
            val nextChar = alphabet.dropWhile { it != char }.firstOrNull { sections.containsKey(it) }
            sections[nextChar]
        }

        if (targetIndex != null) {
            scope.launch {
                // Adjust for headers!
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
            .width(48.dp)
            .fillMaxHeight()
            .padding(top = 32.dp, bottom = 8.dp) // 底部間距由外部 modifier 處理
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
                        fontSize = 10.sp,
                        color = if (activeChar == char) MaterialTheme.colorScheme.primary
                                else if (isPresent) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        fontWeight = if (activeChar == char) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isDragging && activeChar != null,
            enter = fadeIn() + androidx.compose.animation.scaleIn(),
            exit = fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 50.dp)
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

@Composable
fun RecentlyAddedRow(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Recently Added",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs) { song ->
                RecentlyAddedItem(song = song, onClick = { onSongClick(song) })
            }
        }
    }
}

@Composable
fun RecentlyAddedItem(
    song: Song,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp) // Slightly larger
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.Album)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


