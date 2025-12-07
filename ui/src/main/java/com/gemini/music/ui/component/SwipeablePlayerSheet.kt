package com.gemini.music.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class PlayerSheetValue {
    Collapsed,
    Expanded
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeablePlayerSheet(
    state: AnchoredDraggableState<PlayerSheetValue>,
    modifier: Modifier = Modifier,
    expandedContent: @Composable BoxScope.() -> Unit,
    miniPlayerContent: @Composable BoxScope.() -> Unit
) {
    val offset by remember { derivedStateOf { state.requireOffset() } }
    val collapsedOffset = state.anchors.positionOf(PlayerSheetValue.Collapsed)

    // Calculate progress: 0f (Collapsed) -> 1f (Expanded)
    val progress by remember {
        derivedStateOf {
            val currentOffset = state.requireOffset()
            if (collapsedOffset.isNaN() || collapsedOffset == 0f) {
                0f
            } else {
                (1f - (currentOffset / collapsedOffset)).coerceIn(0f, 1f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // The Sheet Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offset.roundToInt()) }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Vertical
                )
        ) {
            // FULL PLAYER CONTENT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = progress
                    }
            ) {
                expandedContent()
            }

            // MINI PLAYER CONTENT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // Fixed height for MiniPlayer area
                    .graphicsLayer {
                        alpha = 1f - progress
                    }
            ) {
                miniPlayerContent()
            }
        }
    }
}
