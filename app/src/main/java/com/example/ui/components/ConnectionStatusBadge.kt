package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ConnectionStatus
import com.example.ui.theme.CleanBorder
import com.example.ui.theme.CleanDeepNavy
import com.example.ui.theme.CleanPillGrey
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusConnecting
import com.example.ui.theme.StatusDisconnected
import com.example.ui.theme.StatusReconnecting

@Composable
fun ConnectionStatusBadge(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val (color, text) = when (status) {
        is ConnectionStatus.Connected -> Pair(StatusConnected, "Connected • 1:1 Live")
        is ConnectionStatus.Connecting -> Pair(StatusConnecting, "Connecting…")
        is ConnectionStatus.WaitingForPeer -> Pair(StatusConnecting, "Waiting for Peer…")
        is ConnectionStatus.Reconnecting -> Pair(StatusReconnecting, "Reconnecting (${status.attempt}/${status.maxAttempts})")
        is ConnectionStatus.GeneratingCode -> Pair(StatusConnecting, "Creating Room…")
        is ConnectionStatus.Ended -> Pair(StatusDisconnected, "Session Ended")
        is ConnectionStatus.Disconnected -> Pair(StatusDisconnected, "Disconnected")
    }

    val isBlinking = status !is ConnectionStatus.Connected && status !is ConnectionStatus.Disconnected

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CleanSurface)
            .border(1.dp, CleanBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isBlinking) color.copy(alpha = alphaAnim) else color)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = CleanDeepNavy
        )
    }
}
