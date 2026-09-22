package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.WarmGold
import kotlinx.coroutines.delay

data class SponsorAd(
    val brandName: String,
    val headline: String,
    val callToAction: String,
    val accentColor: Color
)

@Composable
fun NonIntrusiveBannerAd(
    onAdClick: (SponsorAd) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    var currentAdIndex by remember { mutableIntStateOf(0) }

    val ads = remember {
        listOf(
            SponsorAd(
                brandName = "Mercado Pago",
                headline = "Recibe tus retiros al instante a tu cuenta CLABE sin comisiones",
                callToAction = "Conectar",
                accentColor = NeonCyan
            ),
            SponsorAd(
                brandName = "Nu México",
                headline = "Tu saldo de Watch-to-Earn rinde 15% anual en cajitas Nu",
                callToAction = "Ver más",
                accentColor = NeonPurple
            ),
            SponsorAd(
                brandName = "SPEI Directo",
                headline = "Dispersiones 24/7 avaladas por Banco de México en segundos",
                callToAction = "Activar",
                accentColor = WarmGold
            )
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(8000)
            currentAdIndex = (currentAdIndex + 1) % ads.size
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val currentAd = ads[currentAdIndex]

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xCC0D0F16))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            currentAd.accentColor.copy(alpha = 0.5f),
                            Color(0x33FFFFFF),
                            NeonPurple.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onAdClick(currentAd) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("non_intrusive_banner_ad")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small "AD / Patrocinado" badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(currentAd.accentColor.copy(alpha = 0.2f))
                            .border(0.8.dp, currentAd.accentColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AD",
                            color = currentAd.accentColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = currentAd.brandName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = " • ",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )

                    Text(
                        text = currentAd.headline,
                        color = Color(0xFFD1D5DB),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // CTA chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentAd.accentColor.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = currentAd.callToAction,
                        color = currentAd.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = currentAd.accentColor,
                        modifier = Modifier.size(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Dismiss button
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { isVisible = false },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar anuncio",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
