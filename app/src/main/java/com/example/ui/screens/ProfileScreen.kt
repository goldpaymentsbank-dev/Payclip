package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.VideoItem
import com.example.ui.theme.FuturisticBlack
import com.example.ui.theme.FuturisticDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.WarmGold

@Composable
fun ProfileScreen(
    currentUser: UserEntity?,
    balanceMxn: Double,
    userVideos: List<VideoItem>,
    onOpenAuthModal: () -> Unit,
    onOpenSupabaseModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedProfileTab by remember { mutableIntStateOf(0) } // 0 = Mis Videos, 1 = Estadísticas

    val coins = currentUser?.coinBalance ?: (balanceMxn * 100).toLong()
    val username = currentUser?.username ?: "Alex_ReelsMX"
    val email = currentUser?.email ?: "creador@speireels.mx"
    val rankNumber = currentUser?.userRankNumber ?: 48291

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FuturisticBlack)
            .padding(16.dp)
            .testTag("profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Avatar with Neon Ring
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(listOf(NeonCyan, NeonPurple, WarmGold, NeonCyan))
                        )
                        .padding(3.dp),
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
                            text = username.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Verified tick
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                        .border(2.dp, FuturisticBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verificado",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "@$username",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = email,
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Glowing VIP Rank Badge: "Usuario #48,291 de 1,000,000"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2A1900), Color(0xFF1E0A3C), Color(0xFF002733))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(WarmGold, NeonPurple, NeonCyan)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clickable { onOpenAuthModal() }
                    .testTag("profile_vip_badge")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Usuario Pionero #$rankNumber de 1,000,000 • Bono $150 MXN",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Stats Counters: Seguidores, Me Gusta, Monedas, Saldo MXN
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(FuturisticDarkSurface)
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatMetricItem(value = "4.2K", label = "Seguidores")
                    StatMetricItem(value = "${userVideos.size}", label = "Videos")
                    StatMetricItem(value = "$coins", label = "Monedas", accentColor = WarmGold)
                    StatMetricItem(
                        value = "$${String.format(java.util.Locale.US, "%.0f", balanceMxn)}",
                        label = "Saldo MXN",
                        accentColor = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Actions: Cambiar Cuenta / Modal Bono
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenAuthModal,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("switch_account_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E2C)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cambiar Cuenta",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onOpenAuthModal,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("view_welcome_bonus_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1C08)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmGold)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = WarmGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bono 1M",
                            color = WarmGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Supabase Cloud Hub launcher button
            Button(
                onClick = onOpenSupabaseModal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("open_supabase_hub_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10251E)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(Color(0xFF3ECF8E), NeonCyan))
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = Color(0xFF3ECF8E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ Supabase Cloud Hub (PostgREST & Tareas)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tabs: Mis Videos / Beneficios
        item {
            TabRow(
                selectedTabIndex = selectedProfileTab,
                containerColor = Color(0xFF10121A),
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedProfileTab]),
                        color = NeonCyan,
                        height = 2.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedProfileTab == 0,
                    onClick = { selectedProfileTab = 0 },
                    text = {
                        Text(
                            "Mis Videos Subidos",
                            fontSize = 12.sp,
                            fontWeight = if (selectedProfileTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedProfileTab == 0) NeonCyan else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedProfileTab == 1,
                    onClick = { selectedProfileTab = 1 },
                    text = {
                        Text(
                            "Incentivos & Niveles",
                            fontSize = 12.sp,
                            fontWeight = if (selectedProfileTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedProfileTab == 1) NeonCyan else Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (selectedProfileTab == 0) {
            if (userVideos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FuturisticDarkSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aún no has publicado videos",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dirígete a la pestaña 'Subir (+)' para subir contenido con moderación por IA.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(userVideos) { video ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FuturisticDarkSurface)
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${video.likesCount} me gusta • ${video.commentsCount} comentarios",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0A2E1A))
                                    .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Monetizando",
                                    color = Color(0xFF00E676),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Incentives / Perks Info Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FuturisticDarkSurface)
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Beneficios del Programa Watch-to-Earn",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PerkRow("⚡ Retiros SPEI al instante 24/7 sin intermediarios")
                        PerkRow("🎁 Bono de bienvenida exclusivo de $150 MXN")
                        PerkRow("💎 Gana monedas por cada 25 segundos de video visto")
                        PerkRow("🧠 Recomendaciones inteligentes 'Para Ti' con Gemini 2.5 Flash")
                        PerkRow("🛡️ Moderación segura automatizada con Google AI Studio")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricItem(
    value: String,
    label: String,
    accentColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PerkRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color(0xFFD1D5DB),
            fontSize = 11.sp
        )
    }
}
