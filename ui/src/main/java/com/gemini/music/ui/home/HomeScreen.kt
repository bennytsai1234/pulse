package com.gemini.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gemini.music.domain.model.Song
import com.gemini.music.ui.component.EmptyState
import com.gemini.music.ui.component.SongListItem
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onAlbumsClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Trigger scan when screen is first composed
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.scanMusic()
    }

    // Back Handler for Selection Mode or Drawer
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
                    stringResource(com.gemini.music.ui.R.string.library),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    style = MaterialTheme.typography.titleSmall
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(com.gemini.music.ui.R.string.albums)) },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onAlbumsClick()
                    },
                    icon = { Icon(Icons.Rounded.Folder, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(com.gemini.music.ui.R.string.playlists)) },
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
                NavigationDrawerItem(
                    label = { Text("Recently Added") },
                    selected = false,
                    onClick = {
                        // TODO: Navigate to Recently Added screen or show in drawer
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Rounded.Folder, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                // Add more items...
                Spacer(Modifier.height(24.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(com.gemini.music.ui.R.string.settings)) },
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
                // Dialog
                if (uiState.showAddToPlaylistDialog) {
                    com.gemini.music.ui.component.AddToPlaylistDialog(
                        playlists = uiState.playlists,
                        onDismiss = { viewModel.dismissAddToPlaylistDialog() },
                        onPlaylistSelected = { playlist -> viewModel.addSelectedToPlaylist(playlist) },
                        onCreateNewPlaylist = { name -> viewModel.createPlaylist(name) }
                    )
                }

                Column {
                    // Removed RecentlyAdded from main view - now in drawer menu

                    // Second Row: Controls
                    ControlRow(
                        songCount = uiState.songs.size,
                        currentSortOption = uiState.sortOption,
                        onShuffleClick = { viewModel.shuffleAll() },
                        onSortOptionSelected = { viewModel.setSortOption(it) },
                        onSelectModeClick = { viewModel.enterSelectionMode() }
                    )

                    if (uiState.songs.isEmpty() && !uiState.isLoading) {
                        EmptyState(
                            icon = androidx.compose.material.icons.Icons.Rounded.Menu, // Placeholder
                            title = stringResource(com.gemini.music.ui.R.string.no_songs),
                            message = stringResource(com.gemini.music.ui.R.string.no_songs_message)
                        )
                    } else {
                        SongList(
                            songs = uiState.songs,
                            selectedIds = uiState.selectedSongIds,
                            isSelectionMode = uiState.isSelectionMode,
                            listState = listState,
                            onSongClick = { song ->
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleSongSelection(song.id)
                                } else {
                                    viewModel.playSong(song)
                                    onSongClick(song)
                                }
                            },
                            onSongLongClick = { song ->
                                if (!uiState.isSelectionMode) {
                                    viewModel.enterSelectionMode()
                                    viewModel.toggleSongSelection(song.id)
                                }
                            }
                        )
                    }
                }
                
                // A-Z Scroller Overlay
                if (uiState.songs.isNotEmpty()) {
                    FastScroller(
                        listState = listState,
                        songs = uiState.songs,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
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
    // Use compact TopAppBar by setting smaller height
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation Icon
            if (isSelectionMode) {
                IconButton(onClick = onCloseSelection) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            } else {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                }
            }
            
            // Title
            Text(
                text = if (isSelectionMode) stringResource(com.gemini.music.ui.R.string.selected_count, selectedCount) else stringResource(com.gemini.music.ui.R.string.songs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            // Actions
            if (isSelectionMode) {
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
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search")
                }
            }
        }
    }
}

@Composable
fun ControlRow(
    songCount: Int,
    currentSortOption: SortOption,
    onShuffleClick: () -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onSelectModeClick: () -> Unit
) {
    var showSortMenu by remember { androidx.compose.runtime.mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Shuffle & Count
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
                Text(stringResource(com.gemini.music.ui.R.string.shuffle), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "$songCount " + stringResource(com.gemini.music.ui.R.string.songs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right: Sort & Select
        Row {
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
                                    SortOption.TITLE -> stringResource(com.gemini.music.ui.R.string.sort_title)
                                    SortOption.ARTIST -> stringResource(com.gemini.music.ui.R.string.sort_artist)
                                    SortOption.ALBUM -> stringResource(com.gemini.music.ui.R.string.sort_album)
                                    SortOption.DATE_ADDED -> stringResource(com.gemini.music.ui.R.string.sort_date_added)
                                    SortOption.DURATION -> stringResource(com.gemini.music.ui.R.string.sort_duration)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongList(
    songs: List<Song>,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    listState: LazyListState,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 100.dp, end = 32.dp), // Space for MiniPlayer & FastScroller
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = songs,
            key = { it.id } // Performance optimization
        ) { song ->
            val isSelected = selectedIds.contains(song.id)
            
            SongListItem(
                song = song,
                isSelected = isSelected,
                isSelectionMode = isSelectionMode,
                onClick = { onSongClick(song) },
                onLongClick = { onSongLongClick(song) },
                modifier = Modifier.animateItemPlacement(
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            )
        }
    }
}

// SongListItem moved to ui/component/SongListItem.kt

@Composable
fun FastScroller(
    listState: LazyListState,
    songs: List<Song>,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // Get available letters from songs
    val availableLetters = remember(songs) {
        songs.mapNotNull { song ->
            song.title.firstOrNull()?.uppercaseChar()?.takeIf { it.isLetter() }
        }.distinct().sorted()
    }
    
    // If no letters available, don't show scroller
    if (availableLetters.isEmpty()) return
    
    Column(
        modifier = modifier
            .width(24.dp)
            .fillMaxHeight()
            .padding(top = 8.dp, bottom = 8.dp, end = 4.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), 
                RoundedCornerShape(12.dp)
            ),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        availableLetters.forEach { char ->
            Text(
                text = char.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = if (availableLetters.size > 20) 9.sp else 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .clickable {
                        // Find index of first song starting with this char
                        val index = songs.indexOfFirst { 
                            it.title.firstOrNull()?.uppercaseChar() == char 
                        }
                        if (index != -1) {
                            scope.launch { listState.scrollToItem(index) }
                        }
                    }
            )
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
