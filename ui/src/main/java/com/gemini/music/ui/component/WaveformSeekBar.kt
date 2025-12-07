package com.gemini.music.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * 模擬波形進度條。
 * 在 MVP 階段，我們使用隨機生成的振幅來模擬視覺效果。
 * 後期應從音訊檔案解碼實際振幅數據。
 */
@Composable
fun WaveformSeekBar(
    progress: Float, // 0f to 1f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    waveform: List<Float> = emptyList(),
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.3f)
) {
    // Falls back to random if waveform is empty (e.g. loading or error)
    val randomData = remember { List(50) { Random.nextFloat() * 0.6f + 0.2f } }
    val amplitudes = if (waveform.isNotEmpty()) waveform else randomData
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp) // 增加觸控區域
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onValueChange(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    onValueChange(newProgress)
                }
            }
    ) {
        val barWidth = size.width / amplitudes.size
        val gap = barWidth * 0.3f
        val actualBarWidth = barWidth - gap
        val centerY = size.height / 2

        val activeBrush = Brush.linearGradient(
            colors = listOf(activeColor.copy(alpha = 0.7f), activeColor)
        )

        amplitudes.forEachIndexed { index, amplitude ->
            val barProgress = index.toFloat() / amplitudes.size
            val isPlayed = barProgress <= progress
            
            val barHeight = size.height * amplitude * 0.8f
            val startX = index * barWidth + gap / 2
            
            if (isPlayed) {
                drawLine(
                    brush = activeBrush,
                    start = Offset(startX, centerY - barHeight / 2),
                    end = Offset(startX, centerY + barHeight / 2),
                    strokeWidth = actualBarWidth,
                    cap = StrokeCap.Round
                )
            } else {
                drawLine(
                    color = inactiveColor,
                    start = Offset(startX, centerY - barHeight / 2),
                    end = Offset(startX, centerY + barHeight / 2),
                    strokeWidth = actualBarWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
