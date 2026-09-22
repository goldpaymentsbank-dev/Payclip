package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.FloatingCoinRingMeter
import com.example.ui.components.NonIntrusiveBannerAd
import com.example.ui.components.RewardCoinBadge
import com.example.ui.theme.FuturisticBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.WarmGold
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFeedScreen(
    videos: List<VideoItem>,
    currentIndex: Int,
    watchProgress: Float,
    balance: Double,
    coinRewardPopup: Double?,
    onVideoChange: (Int) -> Unit,
    onLikeToggle: (String) -> Unit,
    onCommentSubmit: (String, String) -> Unit,
    onOpenTrivia: () -> Unit,
    onOpenGifting: () -> Unit,
    onOpenWithdrawal: () -> Unit,
    onRequestAiReorder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVideo = videos.getOrNull(currentIndex) ?: return
    var showCommentsSheet by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var isFollowing by remember { mutableStateOf(false) }

    // Vinyl Disc Rotation Animation
    val infiniteTransition = rememberInfiniteTransition(label = "discRotation")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "discRotate"
    )

    // Pulse Rhythm for visual audio canvas
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FuturisticBlack)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag < -60f && currentIndex < videos.size - 1) {
                            onVideoChange(currentIndex + 1)
                        } else if (totalDrag > 60f && currentIndex > 0) {
                            onVideoChange(currentIndex - 1)
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
            .testTag("video_feed_screen")
    ) {
        // High-Energy Dynamic Video Visuals Canvas (Pitch Black + Neon Beats)
        FuturisticVideoCanvas(
            video = currentVideo,
            pulse = pulseScale,
            modifier = Modifier.fillMaxSize()
        )

        // Top Gradient Vignette
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xE6080808), Color(0x99080808), Color.Transparent)
                    )
                )
        )

        // Bottom Gradient Vignette
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC080808), Color(0xF2080808))
                    )
                )
        )

        // Top Bar HUD: Floating Coin Ring Meter & Feed Tabs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Floating Coin Ring Meter in top corner
                FloatingCoinRingMeter(
                    progress = watchProgress,
                    coinBalance = (balance * 100).toLong(),
                    mxnBalance = balance,
                    onClick = onOpenWithdrawal
                )

                // Category Switcher
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Siguiendo",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Para Ti",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                // AI Recommendation Chip (Gemini 2.5 Flash)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161026))
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NeonCyan, NeonPurple)),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onRequestAiReorder() }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                        .testTag("gemini_reorder_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI",
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Para Ti IA",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Non-Intrusive Sponsor Banner Ad at the top
            NonIntrusiveBannerAd(
                onAdClick = { onOpenWithdrawal() }
            )
        }

        // Floating Coin Reward Badge popup
        RewardCoinBadge(
            visible = coinRewardPopup != null,
            amount = coinRewardPopup ?: 1.50,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
        )

        // Right Side HUD: Glassmorphism Action Cards (Like, Comment, Share, Avatar, Disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Creator Avatar with Follow Button
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(listOf(NeonCyan, NeonPurple, WarmGold, NeonCyan))
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF10121C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentVideo.creatorName.take(1),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (!isFollowing) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                            .clickable { isFollowing = true }
                            .testTag("follow_creator_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Seguir",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Glassmorphism Like Action
            GlassActionItem(
                icon = if (currentVideo.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = "${currentVideo.likesCount}",
                tint = if (currentVideo.isLiked) NeonPink else Color.White,
                onClick = { onLikeToggle(currentVideo.id) },
                testTag = "like_button"
            )

            // Glassmorphism Comment Action
            GlassActionItem(
                icon = Icons.Default.Comment,
                label = "${currentVideo.commentsCount}",
                tint = Color.White,
                onClick = { showCommentsSheet = true },
                testTag = "comment_button"
            )

            // Glassmorphism Live Gift Action (Direct Creator Monetization)
            GlassActionItem(
                icon = Icons.Default.CardGiftcard,
                label = "Regalar",
                tint = WarmGold,
                onClick = onOpenGifting,
                testTag = "gift_button"
            )

            // Glassmorphism Share Action
            GlassActionItem(
                icon = Icons.Default.Share,
                label = "${currentVideo.sharesCount}",
                tint = Color.White,
                onClick = {},
                testTag = "share_button"
            )

            // Rotating Vinyl Sound Disc
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161922))
                    .border(2.dp, Color(0x44FFFFFF), CircleShape)
                    .rotate(discRotation),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                )
            }
        }

        // Bottom Left HUD: Creator Info, Description & Sponsored Trivia Callout
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 16.dp, bottom = 28.dp)
        ) {
            // Sponsored Trivia Banner (Direct Cash Incentive)
            if (currentVideo.trivia != null && !currentVideo.hasAnsweredTrivia) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF2E1902), Color(0xFF162534))
                            )
                        )
                        .border(1.dp, WarmGold, RoundedCornerShape(12.dp))
                        .clickable { onOpenTrivia() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("open_trivia_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Trivia",
                            tint = WarmGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trivia: ¡Gana +$${String.format(java.util.Locale.US, "%.0f", currentVideo.trivia.rewardMxn)} MXN aquí!",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Creator Handle & Verified Tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentVideo.creatorName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentVideo.creatorHandle,
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Video Description
            Text(
                text = currentVideo.description,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 2,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sound Track info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currentVideo.soundTitle,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }

    // Comments Sheet
    if (showCommentsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF0F121C),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comentarios (${currentVideo.comments.size})",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showCommentsSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(currentVideo.comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF222838)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.authorName.take(1),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.authorName,
                                        color = NeonCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = comment.timeAgo,
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = comment.text,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Añadir un comentario...", color = Color.Gray, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onCommentSubmit(currentVideo.id, newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x770D0E16))
                .border(1.dp, Color(0x33FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FuturisticVideoCanvas(
    video: VideoItem,
    pulse: Float,
    modifier: Modifier = Modifier
) {
    val accentColor = when (video.videoTheme) {
        "fintech" -> NeonCyan
        "gaming" -> NeonPink
        "tech" -> NeonPurple
        "food" -> WarmGold
        else -> NeonCyan
    }

    val gradientColors = listOf(
        Color(0xFF080808),
        Color(0xFF0F101A),
        accentColor.copy(alpha = 0.12f),
        Color(0xFF080808)
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = gradientColors,
                startY = 0f,
                endY = height
            )
        )

        val centerX = width / 2f
        val centerY = height / 2.2f

        // Expanding Sound Beat Circles
        drawCircle(
            color = accentColor.copy(alpha = 0.08f),
            radius = (width * 0.44f) * pulse,
            center = Offset(centerX, centerY)
        )

        drawCircle(
            color = accentColor.copy(alpha = 0.18f),
            radius = (width * 0.28f) * pulse,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Radial Wave Visualizer Lines
        val linesCount = 28
        for (i in 0 until linesCount) {
            val angle = (i * 360f / linesCount) * (Math.PI / 180f)
            val baseRadius = (width * 0.28f) * pulse
            val waveLength = (12f + (i % 6) * 14f) * pulse

            val startX = centerX + (baseRadius * cos(angle)).toFloat()
            val startY = centerY + (baseRadius * sin(angle)).toFloat()
            val endX = centerX + ((baseRadius + waveLength) * cos(angle)).toFloat()
            val endY = centerY + ((baseRadius + waveLength) * sin(angle)).toFloat()

            drawLine(
                color = accentColor.copy(alpha = 0.45f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
