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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CepReceipt
import com.example.data.model.SpeiAccountEntity
import com.example.data.model.TransactionType
import com.example.data.model.WalletTransactionEntity
import com.example.data.spei.SpeiEngine
import com.example.ui.theme.BanxicoGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SpeiGold
import com.example.ui.theme.SpeiGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    balance: Double,
    todayWithdrawn: Double,
    transactions: List<WalletTransactionEntity>,
    savedAccounts: List<SpeiAccountEntity>,
    onOpenWithdrawal: () -> Unit,
    onShowCepReceipt: (CepReceipt) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "SPEI" -> transactions.filter { it.type == TransactionType.SPEI_WITHDRAWAL }
            "PREMIOS" -> transactions.filter {
                it.type == TransactionType.WATCH_REWARD ||
                        it.type == TransactionType.TRIVIA_BONUS ||
                        it.type == TransactionType.CHALLENGE_WIN
            }
            "REGALOS" -> transactions.filter {
                it.type == TransactionType.LIVE_GIFT_RECEIVED || it.type == TransactionType.LIVE_GIFT_SENT
            }
            else -> transactions
        }
    }

    val dailyLimitProgress = (todayWithdrawn / SpeiEngine.MAX_DAILY_LIMIT_MXN).toFloat().coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0F15))
            .padding(horizontal = 16.dp)
            .testTag("wallet_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BILLETERA & PAGOS SPEI",
                        color = SpeiGreenPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Tus Recompensas Instantáneas",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF14241B))
                        .border(1.dp, SpeiGreenPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SpeiGreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SPEI 24/7 Activo",
                            color = SpeiGreenPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Balance Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0F2618), Color(0xFF142B23), Color(0xFF0E1A2B))
                        )
                    )
                    .border(1.5.dp, SpeiGreenPrimary.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saldo total disponible",
                            color = Color(0xFFA7F3D0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Seguro",
                                tint = SpeiGreenPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STP Banxico",
                                color = SpeiGreenPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$",
                            color = SpeiGreenPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%.2f", balance),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " MXN",
                            color = SpeiGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Cashout Action
                    Button(
                        onClick = onOpenWithdrawal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("retirar_spei_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpeiGreenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "SPEI",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cobrar por SPEI al Instante",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Transferencia directa a cualquier CLABE de 18 dígitos en menos de 5 segundos.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anti-Fraud Daily Limit Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131A24))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Anti-Fraude",
                                tint = SpeiGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Límite Diario Anti-Fraude",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "$${String.format("%.0f", todayWithdrawn)} / $${String.format("%.0f", SpeiEngine.MAX_DAILY_LIMIT_MXN)} MXN",
                            color = SpeiGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { dailyLimitProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SpeiGreenPrimary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Mínimo de retiro: $${String.format("%.0f", SpeiEngine.MIN_WITHDRAWAL_MXN)} MXN • Protección contra ataques y bots",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved Accounts Banner
            if (savedAccounts.isNotEmpty()) {
                Text(
                    text = "Tus Cuentas Bancarias para SPEI",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                savedAccounts.forEach { acc ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF161E2A))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SpeiGreenPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = acc.bankName,
                                    tint = SpeiGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = acc.bankName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "CLABE: ${acc.clabe.take(3)} •••• ${acc.clabe.takeLast(4)} • ${acc.beneficiaryName}",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BanxicoGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Verificada", color = SpeiGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Transaction History Header & Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = SpeiGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Historial de Movimientos",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${filteredTransactions.size} registros",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf("TODOS", "SPEI", "PREMIOS", "REGALOS")
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpeiGreenPrimary,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E2634),
                            labelColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Transaction List Items
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay transacciones en este filtro.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                TransactionCardItem(
                    transaction = tx,
                    onViewCep = {
                        val cep = SpeiEngine.generateCepReceipt(
                            trackingKey = if (tx.trackingKey.isNotEmpty()) tx.trackingKey else SpeiEngine.generateTrackingKey(),
                            amountMxn = tx.amountMxn,
                            beneficiaryName = "Carlos Mendoza Rivera",
                            beneficiaryClabe = if (tx.targetClabe.isNotEmpty()) tx.targetClabe else "012180015498263828",
                            bankName = if (tx.bankName.isNotEmpty()) tx.bankName else "BBVA México",
                            concept = tx.sanitizedConcept.ifEmpty { "RETIRO SPEI REELS" }
                        )
                        onShowCepReceipt(cep)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TransactionCardItem(
    transaction: WalletTransactionEntity,
    onViewCep: () -> Unit
) {
    val isWithdrawal = transaction.type == TransactionType.SPEI_WITHDRAWAL
    val isExpense = isWithdrawal || transaction.type == TransactionType.LIVE_GIFT_SENT

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "MX"))
    val formattedDate = dateFormat.format(Date(transaction.timestamp))

    val icon = when (transaction.type) {
        TransactionType.SPEI_WITHDRAWAL -> Icons.Default.FlashOn
        TransactionType.WATCH_REWARD -> Icons.Default.PlayCircleOutline
        TransactionType.TRIVIA_BONUS -> Icons.Default.Help
        TransactionType.CHALLENGE_WIN -> Icons.Default.MonetizationOn
        TransactionType.LIVE_GIFT_RECEIVED -> Icons.Default.ArrowDownward
        TransactionType.LIVE_GIFT_SENT -> Icons.Default.ArrowUpward
    }

    val iconColor = when (transaction.type) {
        TransactionType.SPEI_WITHDRAWAL -> SpeiGreenPrimary
        TransactionType.WATCH_REWARD -> SpeiGold
        TransactionType.TRIVIA_BONUS -> NeonCyan
        TransactionType.CHALLENGE_WIN -> SpeiGold
        TransactionType.LIVE_GIFT_RECEIVED -> SpeiGreenPrimary
        TransactionType.LIVE_GIFT_SENT -> NeonPink
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141A24))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            .clickable(enabled = isWithdrawal) { onViewCep() }
            .padding(14.dp)
            .testTag("tx_item_${transaction.id}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = formattedDate,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                if (isWithdrawal && transaction.trackingKey.isNotEmpty()) {
                    Text(
                        text = "Rastreo: ${transaction.trackingKey}",
                        color = SpeiGreenPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isExpense) "-" else "+"}$${String.format("%.2f", transaction.amountMxn)}",
                    color = if (isExpense) Color.White else SpeiGreenPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (isWithdrawal) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BanxicoGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Ver CEP",
                                tint = SpeiGreenPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Ver CEP",
                                color = SpeiGreenPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Acreditado",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
