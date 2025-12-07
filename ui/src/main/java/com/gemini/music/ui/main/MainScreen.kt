package com.gemini.music.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
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
import com.gemini.music.ui.nowplaying.QueueSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val musicState by viewModel.musicState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val waveform by viewModel.waveform.collectAsState()

    var showQueue by remember { mutableStateOf(false) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isPlayerVisible = currentRoute != Screen.Settings.route && currentRoute != Screen.Search.route

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    if (showQueue) {
        QueueSheet(
            queue = musicState.queue,
            currentSong = musicState.currentSong,
            onDismissRequest = { showQueue = false },
            onPlaySong = { viewModel.playQueueItem(it) },
            onRemoveSong = { viewModel.removeQueueItem(it) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val availableHeight = maxHeight
            val sheetAnchors = remember(availableHeight, density) {
                DraggableAnchors {
                    PlayerSheetValue.Collapsed at with(density) { (availableHeight - 80.dp).toPx() }
                    PlayerSheetValue.Expanded at 0f
                }
            }

            val sheetState = remember(sheetAnchors) {
                AnchoredDraggableState(
                    initialValue = PlayerSheetValue.Collapsed,
                    anchors = sheetAnchors,
                    positionalThreshold = { distance: Float -> distance * 0.5f },
                    velocityThreshold = { with(density) { 100.dp.toPx() } },
                    animationSpec = tween(300)
                )
            }

            MusicNavigation(navController = navController)

            if (isPlayerVisible) {
                Spacer(modifier = Modifier.height(80.dp).align(Alignment.BottomStart).fillMaxWidth())

                SwipeablePlayerSheet(
                    state = sheetState,
                    miniPlayerContent = {
                        MiniPlayer(
                            song = musicState.currentSong,
                            isPlaying = musicState.isPlaying,
                            progress = progress,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onQueueClick = { showQueue = true },
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
                            onQueueClick = { showQueue = true },
                            waveform = waveform
                        )
                    }
                )
            }

            BackHandler(enabled = sheetState.currentValue == PlayerSheetValue.Expanded || showQueue) {
                if (showQueue) {
                    showQueue = false
                } else {
                    scope.launch { sheetState.animateTo(PlayerSheetValue.Collapsed) }
                }
            }
        }
    }
}
