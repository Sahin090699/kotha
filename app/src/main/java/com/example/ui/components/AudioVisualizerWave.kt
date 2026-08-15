package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CleanBengaliAccent
import com.example.ui.theme.CleanDeepNavy
import com.example.ui.theme.CleanRecordingPulse

@Composable
fun AudioVisualizerWave(
    isRecording: Boolean,
    isPlaying: Boolean,
    amplitude: Float,
    frequencies: List<Float>,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 56.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activeColor = when {
        isRecording -> CleanRecordingPulse
        isPlaying -> CleanBengaliAccent
        else -> CleanDeepNavy
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            activeColor,
            activeColor.copy(alpha = 0.35f)
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = if (frequencies.isNotEmpty()) frequencies else List(16) { 0.15f }

        bars.forEachIndexed { index, freq ->
            val scale = if (isRecording || isPlaying) {
                (freq * (if (index % 2 == 0) pulseAnim else (2.0f - pulseAnim))).coerceIn(0.12f, 1.0f)
            } else {
                0.12f
            }

            val barHeight = maxHeight * scale

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(gradient)
            )
        }
    }
}
