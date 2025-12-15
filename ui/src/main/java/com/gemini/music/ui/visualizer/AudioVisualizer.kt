package com.gemini.music.ui.visualizer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemini.music.domain.model.VisualizerColorMode
import com.gemini.music.domain.model.VisualizerType
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 音訊視覺化組件
 */
@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier,
    type: VisualizerType = VisualizerType.BARS,
    isPlaying: Boolean = false,
    sensitivity: Float = 1.0f,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    colorMode: VisualizerColorMode = VisualizerColorMode.GRADIENT,
    barCount: Int = 32,
    height: Dp = 120.dp
) {
    // Simulated audio data (in real app, would come from audio session)
    var audioData by remember { mutableStateOf(FloatArray(barCount) { 0f }) }
    
    // Animate data changes
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            audioData = FloatArray(barCount) { 
                (Random.nextFloat() * sensitivity).coerceIn(0f, 1f)
            }
            delay(50) // 20 FPS update
        }
        // Fade out when not playing
        if (!isPlaying) {
            repeat(10) {
                audioData = audioData.map { (it * 0.8f).coerceAtLeast(0.05f) }.toFloatArray()
                delay(50)
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        when (type) {
            VisualizerType.BARS -> BarVisualizer(
                audioData = audioData,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                colorMode = colorMode
            )
            VisualizerType.WAVE -> WaveVisualizer(
                audioData = audioData,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
            VisualizerType.CIRCLE -> CircleVisualizer(
                audioData = audioData,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
            VisualizerType.LINE -> LineVisualizer(
                audioData = audioData,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
            VisualizerType.PARTICLE -> ParticleVisualizer(
                audioData = audioData,
                primaryColor = primaryColor,
                isPlaying = isPlaying
            )
        }
    }
}

@Composable
private fun BarVisualizer(
    audioData: FloatArray,
    primaryColor: Color,
    secondaryColor: Color,
    colorMode: VisualizerColorMode
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = size.width / (audioData.size * 1.5f)
        val gap = barWidth * 0.5f
        val maxHeight = size.height * 0.9f
        
        audioData.forEachIndexed { index, value ->
            val barHeight = (value * maxHeight).coerceAtLeast(4f)
            val x = index * (barWidth + gap) + gap / 2
            val y = (size.height - barHeight) / 2
            
            val color = when (colorMode) {
                VisualizerColorMode.GRADIENT -> {
                    val progress = index.toFloat() / audioData.size
                    lerp(primaryColor, secondaryColor, progress)
                }
                VisualizerColorMode.RAINBOW -> {
                    Color.hsv(
                        hue = (index.toFloat() / audioData.size * 360f),
                        saturation = 0.8f,
                        value = 0.9f
                    )
                }
                else -> primaryColor
            }
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2)
            )
        }
    }
}

@Composable
private fun WaveVisualizer(
    audioData: FloatArray,
    primaryColor: Color,
    secondaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val centerY = size.height / 2
        val amplitude = size.height * 0.35f
        
        path.moveTo(0f, centerY)
        
        for (i in 0 until size.width.toInt() step 2) {
            val progress = i / size.width
            val dataIndex = (progress * audioData.size).toInt().coerceIn(0, audioData.size - 1)
            val audioValue = audioData[dataIndex]
            
            val wave = sin(progress * PI * 4 + phase) * audioValue * amplitude
            path.lineTo(i.toFloat(), centerY + wave.toFloat())
        }
        
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun CircleVisualizer(
    audioData: FloatArray,
    primaryColor: Color,
    secondaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = minOf(size.width, size.height) * 0.25f
        val maxExtend = minOf(size.width, size.height) * 0.2f
        
        audioData.forEachIndexed { index, value ->
            val angle = (index.toFloat() / audioData.size * 360f + rotation) * (PI / 180f)
            val extend = value * maxExtend
            
            val innerRadius = baseRadius
            val outerRadius = baseRadius + extend
            
            val startX = center.x + cos(angle).toFloat() * innerRadius
            val startY = center.y + sin(angle).toFloat() * innerRadius
            val endX = center.x + cos(angle).toFloat() * outerRadius
            val endY = center.y + sin(angle).toFloat() * outerRadius
            
            val progress = index.toFloat() / audioData.size
            val color = lerp(primaryColor, secondaryColor, progress)
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun LineVisualizer(
    audioData: FloatArray,
    primaryColor: Color,
    secondaryColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val stepX = size.width / (audioData.size - 1)
        val centerY = size.height / 2
        val amplitude = size.height * 0.4f
        
        path.moveTo(0f, centerY - audioData[0] * amplitude)
        
        for (i in 1 until audioData.size) {
            val x = i * stepX
            val y = centerY - audioData[i] * amplitude
            
            // Smooth curve using cubic bezier
            val prevX = (i - 1) * stepX
            val prevY = centerY - audioData[i - 1] * amplitude
            val controlX = (prevX + x) / 2
            
            path.cubicTo(
                controlX, prevY,
                controlX, y,
                x, y
            )
        }
        
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Mirror effect
        val mirrorPath = Path()
        mirrorPath.moveTo(0f, centerY + audioData[0] * amplitude * 0.5f)
        
        for (i in 1 until audioData.size) {
            val x = i * stepX
            val y = centerY + audioData[i] * amplitude * 0.5f
            
            val prevX = (i - 1) * stepX
            val prevY = centerY + audioData[i - 1] * amplitude * 0.5f
            val controlX = (prevX + x) / 2
            
            mirrorPath.cubicTo(controlX, prevY, controlX, y, x, y)
        }
        
        drawPath(
            path = mirrorPath,
            brush = Brush.horizontalGradient(
                listOf(primaryColor.copy(alpha = 0.3f), secondaryColor.copy(alpha = 0.3f))
            ),
            style = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ParticleVisualizer(
    audioData: FloatArray,
    primaryColor: Color,
    isPlaying: Boolean
) {
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var alpha: Float
    )
    
    var particles by remember { 
        mutableStateOf(List(50) { 
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.02f,
                vy = (Random.nextFloat() - 0.5f) * 0.02f,
                size = Random.nextFloat() * 8f + 2f,
                alpha = Random.nextFloat() * 0.5f + 0.3f
            )
        })
    }
    
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val avgIntensity = audioData.average().toFloat()
            particles = particles.map { p ->
                var newX = p.x + p.vx * (1 + avgIntensity * 2)
                var newY = p.y + p.vy * (1 + avgIntensity * 2)
                
                // Wrap around
                if (newX < 0) newX = 1f
                if (newX > 1) newX = 0f
                if (newY < 0) newY = 1f
                if (newY > 1) newY = 0f
                
                p.copy(
                    x = newX,
                    y = newY,
                    size = (p.size + avgIntensity * 3).coerceIn(2f, 15f)
                )
            }
            delay(30)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = primaryColor.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x * size.width, particle.y * size.height)
            )
        }
    }
}

// Color interpolation helper
private fun lerp(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}
