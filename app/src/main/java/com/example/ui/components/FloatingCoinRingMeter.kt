package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.WarmGold

/**
 * Floating Coin Ring Meter in the top corner of the vertical video player.
 * Fills up in real-time as a video plays and awards coins upon completion.
 */
@Composable
fun FloatingCoinRingMeter(
    progress: Float, // 0.0f to 1.0f
    coinBalance: Long,
    mxnBalance: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin_rotation")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .testTag("floating_coin_meter")
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xE60D0D12))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = pulseAlpha),
                        NeonPurple.copy(alpha = 0.8f),
                        WarmGold.copy(alpha = pulseAlpha)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress Ring with Spinning Gold Coin Inside
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(38.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1.0f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0x3300F2FE),
                    strokeWidth = 3.dp,
                    trackColor = Color.Transparent
                )
                // Active Animated Progress
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0.02f, 1.0f) },
                    modifier = Modifier.fillMaxSize(),
                    color = WarmGold,
                    strokeWidth = 3.5.dp,
                    trackColor = Color.Transparent
                )
                // Glowing Gold Coin Icon
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Monedas",
                    tint = WarmGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Info: Coins & MXN
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+$coinBalance",
                        color = WarmGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "pts",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$${String.format(java.util.Locale.US, "%.2f", mxnBalance)} MXN",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
