package com.gemini.music.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gemini.music.ui.component.MiniPlayer
import com.gemini.music.ui.component.PlayerSheetValue
import com.gemini.music.ui.component.SwipeablePlayerSheet
import com.gemini.music.ui.navigation.MusicNavigation
import com.gemini.music.ui.navigation.Screen
import com.gemini.music.ui.nowplaying.NowPlayingScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val musicState by viewModel.musicState.collectAsState()
    val progress by viewModel.progress.collectAsState()


    val dynamicThemeState by viewModel.dynamicThemeState.collectAsState()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    // Hide mini player on Settings, Search, and Queue
    val isPlayerVisible = currentRoute != Screen.Settings.route && 
                          currentRoute != Screen.Search.route && 
                          currentRoute != Screen.Queue.route

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val availableHeight = maxHeight
            val sheetAnchors = remember(availableHeight, density) {
                DraggableAnchors {
                    PlayerSheetValue.Collapsed at with(density) { (availableHeight - 72.dp).toPx() }
                    PlayerSheetValue.Expanded at 0f
                }
            }

            val sheetState = remember(sheetAnchors) {
                AnchoredDraggableState(
                    initialValue = PlayerSheetValue.Collapsed,
                    anchors = sheetAnchors,
                    positionalThreshold = { distance: Float -> distance * 0.5f },
                    velocityThreshold = { with(density) { 100.dp.toPx() } },
                    snapAnimationSpec = tween(200),
                    decayAnimationSpec = exponentialDecay()
                )
            }
            
            // Update anchors when availableHeight changes
            androidx.compose.runtime.LaunchedEffect(sheetAnchors) {
                sheetState.updateAnchors(sheetAnchors)
            }

            MusicNavigation(navController = navController)

            if (isPlayerVisible) {
                Spacer(modifier = Modifier.height(72.dp).align(Alignment.BottomStart).fillMaxWidth())

                SwipeablePlayerSheet(
                    state = sheetState,
                    miniPlayerContent = {
                        MiniPlayer(
                            song = musicState.currentSong,
                            isPlaying = musicState.isPlaying,
                            progress = progress,
                            dynamicTheme = dynamicThemeState,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onQueueClick = { 
                                // Navigate to Queue
                                navController.navigate(Screen.Queue.route)
                            },
                            onClick = {
                                scope.launch { sheetState.animateTo(PlayerSheetValue.Expanded) }
                            }
                        )
                    },
                    expandedContent = {
                        NowPlayingScreen(
                            onBackClick = {
                                scope.launch { sheetState.animateTo(PlayerSheetValue.Collapsed) }
                            },
                            onQueueClick = { 
                                // Navigate to Queue and collapse or keep open?
                                // Better to collapse or just navigate on top.
                                // If we navigate, BackHandler of Navigation takes over.
                                navController.navigate(Screen.Queue.route)
                            },
                            onAlbumClick = { albumId ->
                                scope.launch { sheetState.animateTo(PlayerSheetValue.Collapsed) }
                                navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                            }
                        )
                    }
                )
            }

            BackHandler(enabled = sheetState.currentValue == PlayerSheetValue.Expanded) {
                 scope.launch { sheetState.animateTo(PlayerSheetValue.Collapsed) }
            }
        }
    }
}
