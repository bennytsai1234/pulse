package com.pulse.music.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.music.domain.model.LyricLine
import com.pulse.music.domain.model.LyricWord

/**
 * 卡拉OK風格歌詞顯示組件
 * 支援逐字高亮動畫和自動滾動
 */
@Composable
fun KaraokeLyrics(
    lyrics: List<LyricLine>,
    currentPosition: Long,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    hasError: Boolean = false,
    onRetry: () -> Unit = {},
    onClick: () -> Unit = {},
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    normalColor: Color = Color.White.copy(alpha = 0.5f)
) {
    if (isLoading) {
        LoadingLyricsView(modifier)
        return
    }

    if (lyrics.isEmpty()) {
        EmptyLyricsView(
            modifier = modifier,
            hasError = hasError,
            onRetry = onRetry
        )
        return
    }

    val listState = rememberLazyListState()

    // Find current line index
    var currentLineIndex by remember { mutableIntStateOf(-1) }

    val isDragged by listState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(currentPosition, lyrics) {
        if (isDragged) return@LaunchedEffect

        val newIndex = lyrics.indexOfLast { it.startTime <= currentPosition }
        if (newIndex != currentLineIndex && newIndex >= 0) {
            currentLineIndex = newIndex
            // Auto-scroll to current line (centered)
            val targetIndex = (newIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top spacer for centering effect
        item { Spacer(modifier = Modifier.height(100.dp)) }

        itemsIndexed(lyrics) { index, lyricLine ->
            val isCurrentLine = index == currentLineIndex
            val isPastLine = index < currentLineIndex

            LyricLineView(
                lyricLine = lyricLine,
                currentPosition = currentPosition,
                isCurrentLine = isCurrentLine,
                isPastLine = isPastLine,
                highlightColor = highlightColor,
                normalColor = normalColor
            )
        }

        // Bottom spacer
        item { Spacer(modifier = Modifier.height(200.dp)) }
    }
}

@Composable
private fun LyricLineView(
    lyricLine: LyricLine,
    currentPosition: Long,
    isCurrentLine: Boolean,
    isPastLine: Boolean,
    highlightColor: Color,
    normalColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrentLine) 1.3f else 1f,
        animationSpec = tween(200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "LyricScale"
    )

    val alpha by animateFloatAsState(
        targetValue = when {
            isCurrentLine -> 1f
            isPastLine -> 0.3f
            else -> 0.3f
        },
        animationSpec = tween(200, easing = androidx.compose.animation.core.LinearEasing),
        label = "LyricAlpha"
    )

    // Float effect: current line floats up
    val translationY by animateFloatAsState(
        targetValue = if (isCurrentLine) -12f else 0f,
        animationSpec = tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "LyricFloat"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                this.translationY = translationY
                // Use shadowElevation for lift effect
                if (isCurrentLine) {
                    shadowElevation = 8f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (lyricLine.hasWordTimings && isCurrentLine) {
            // Karaoke Mode: Word by word
            KaraokeWordByWord(
                words = lyricLine.words,
                currentPosition = currentPosition,
                highlightColor = highlightColor,
                normalColor = normalColor
            )
        } else {
            // Standard Mode: Line by line
            StandardLyricLine(
                text = lyricLine.text,
                isCurrentLine = isCurrentLine,
                isPastLine = isPastLine,
                highlightColor = highlightColor,
                normalColor = normalColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KaraokeWordByWord(
    words: List<LyricWord>,
    currentPosition: Long,
    highlightColor: Color,
    normalColor: Color
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        words.forEach { word ->
            KaraokeWord(
                word = word,
                currentPosition = currentPosition,
                highlightColor = highlightColor,
                normalColor = normalColor
            )
        }
    }
}

@Composable
private fun KaraokeWord(
    word: LyricWord,
    currentPosition: Long,
    highlightColor: Color,
    normalColor: Color
) {
    val progress = word.getProgress(currentPosition)

    // Animate the progress for smoothness
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 50,
            easing = LinearEasing
        ),
        label = "WordProgress"
    )

    Box {
        // Background text (unhighlighted)
        Text(
            text = word.text,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = normalColor,
                shadow = Shadow(
                    color = highlightColor.copy(alpha = 0.5f),
                    blurRadius = 12f
                )
            )
        )

        // Foreground text (highlighted with clip)
        Text(
            text = word.text,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor,
                shadow = Shadow(
                    color = highlightColor,
                    blurRadius = 24f
                )
            ),
            modifier = Modifier.drawWithContent {
                val clipWidth = size.width * animatedProgress
                drawContent()
                // Draw a mask to clip the highlighted portion
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(clipWidth, 0f),
                    size = Size(size.width - clipWidth, size.height),
                    blendMode = BlendMode.Clear
                )
            }.graphicsLayer {
                // Required for BlendMode.Clear to work
                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
            }
        )
    }
}

@Composable
private fun StandardLyricLine(
    text: String,
    isCurrentLine: Boolean,
    isPastLine: Boolean,
    highlightColor: Color,
    normalColor: Color
) {
    val textColor by animateColorAsState(
        targetValue = when {
            isCurrentLine -> highlightColor
            isPastLine -> highlightColor.copy(alpha = 0.6f)
            else -> normalColor
        },
        animationSpec = tween(150, easing = androidx.compose.animation.core.LinearEasing),
        label = "LineColor"
    )

    Text(
        text = text,
        style = TextStyle(
            fontSize = if (isCurrentLine) 24.sp else 18.sp,
            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            shadow = if (isCurrentLine) Shadow(
                color = highlightColor,
                blurRadius = 24f
            ) else null
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EmptyLyricsView(
    modifier: Modifier = Modifier,
    hasError: Boolean = false,
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "♪",
                fontSize = 48.sp,
                color = Color.White.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasError) "Failed to load lyrics" else "No lyrics available",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.5f)
            )
            if (hasError) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.TextButton(onClick = onRetry) {
                    Text(
                        text = "Tap to Retry",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingLyricsView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Loading lyrics...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}


