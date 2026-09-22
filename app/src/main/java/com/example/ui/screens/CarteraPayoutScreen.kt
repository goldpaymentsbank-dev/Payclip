package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CepReceipt
import com.example.data.model.PayoutRequestEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletTransactionEntity
import com.example.data.spei.SpeiEngine
import com.example.ui.theme.FuturisticBlack
import com.example.ui.theme.FuturisticDarkCard
import com.example.ui.theme.FuturisticDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.WarmGold

@Composable
fun CarteraPayoutScreen(
    balanceMxn: Double,
    currentUser: UserEntity?,
    transactions: List<WalletTransactionEntity>,
    payoutRequests: List<PayoutRequestEntity>,
    withdrawalState: String?, // "Pending", "Processing", "Success"
    onExecuteWithdrawal: (method: String, amount: Double, destination: String, holderName: String) -> Unit,
    onViewCepReceipt: (CepReceipt) -> Unit,
    onOpenBonusModal: () -> Unit,
    onOpenSupabaseCloud: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMethodTab by remember { mutableIntStateOf(0) } // 0 = Mercado Pago, 1 = PayPal, 2 = SPEI Banco

    // Form states
    var mpFullName by remember { mutableStateOf("Carlos Mendoza Rivera") }
    var mpAccountInput by remember { mutableStateOf("012180015498263828") } // CLABE or phone/email

    var paypalEmail by remember { mutableStateOf("carlos.mendoza@payouts.net") }

    var speiClabe by remember { mutableStateOf("012180015498263828") }
    var speiBeneficiary by remember { mutableStateOf("Carlos Mendoza Rivera") }

    var withdrawalAmountInput by remember { mutableStateOf("150.00") }
    var withdrawalFeedback by remember { mutableStateOf<String?>(null) }

    val coinsBalance = currentUser?.coinBalance ?: (balanceMxn * 100).toLong()
    val balanceUsd = balanceMxn / 18.0

    val infiniteTransition = rememberInfiniteTransition(label = "gold_glow")
    val goldPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gold_pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FuturisticBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("cartera_payout_screen")
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WarmGold.copy(alpha = 0.2f))
                            .border(1.dp, WarmGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Cartera",
                            tint = WarmGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cartera & Payouts",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Retiros Instantáneos 24/7",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Supabase Cloud Hub Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F231D))
                            .border(1.dp, Color(0xFF3ECF8E), RoundedCornerShape(12.dp))
                            .clickable { onOpenSupabaseCloud() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("cartera_supabase_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Supabase",
                                tint = Color(0xFF3ECF8E),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Supabase",
                                color = Color(0xFF3ECF8E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Rank badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF191928))
                            .border(1.dp, NeonPurple, RoundedCornerShape(12.dp))
                            .clickable { onOpenBonusModal() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = WarmGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VIP #48,291",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Welcome Bonus Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2E1903), Color(0xFF1E0A3C), Color(0xFF002733))
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.horizontalGradient(
                            listOf(
                                WarmGold.copy(alpha = goldPulse),
                                NeonPurple.copy(alpha = goldPulse),
                                NeonCyan.copy(alpha = goldPulse)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onOpenBonusModal() }
                    .padding(12.dp)
                    .testTag("cartera_bonus_banner")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎉", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Bono de $150 MXN activo por ser de los primeros 1,000,000 de usuarios!",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Acreditado a tu saldo disponible para retiro directo.",
                            color = WarmGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Live Balance Card (MXN / USD / Coins)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(FuturisticDarkSurface)
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0x4400F2FE), Color(0x44FFB703))),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
                    .testTag("live_balance_card")
            ) {
                Column {
                    Text(
                        text = "SALDO DISPONIBLE PARA RETIRO",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format(java.util.Locale.US, "%.2f", balanceMxn)}",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MXN",
                                    color = NeonCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "≈ $${String.format(java.util.Locale.US, "%.2f", balanceUsd)} USD",
                                color = Color(0xFFA0A5B5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Coins total pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(WarmGold.copy(alpha = 0.15f))
                                .border(1.dp, WarmGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = WarmGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$coinsBalance pts",
                                    color = WarmGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x22FFFFFF))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tasa de cambio: 100 Monedas = $1.00 MXN",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Sin comisión de retiro",
                            color = Color(0xFF00E676),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Payout Method Tabs (Mercado Pago, PayPal, SPEI)
        item {
            Text(
                text = "Selecciona Método de Retiro:",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedMethodTab,
                containerColor = Color(0xFF10121A),
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedMethodTab]),
                        color = NeonCyan,
                        height = 2.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedMethodTab == 0,
                    onClick = { selectedMethodTab = 0 },
                    text = {
                        Text(
                            "Mercado Pago",
                            fontSize = 11.sp,
                            fontWeight = if (selectedMethodTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMethodTab == 0) NeonCyan else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedMethodTab == 1,
                    onClick = { selectedMethodTab = 1 },
                    text = {
                        Text(
                            "PayPal",
                            fontSize = 11.sp,
                            fontWeight = if (selectedMethodTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMethodTab == 1) NeonCyan else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedMethodTab == 2,
                    onClick = { selectedMethodTab = 2 },
                    text = {
                        Text(
                            "SPEI Banco",
                            fontSize = 11.sp,
                            fontWeight = if (selectedMethodTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMethodTab == 2) NeonCyan else Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Payment Form Inputs Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(FuturisticDarkSurface)
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
                    .padding(16.dp)
                    .testTag("payout_form_card")
            ) {
                Column {
                    when (selectedMethodTab) {
                        0 -> { // Mercado Pago
                            Text(
                                text = "Retiro Directo a Mercado Pago",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = mpFullName,
                                onValueChange = { mpFullName = it },
                                label = { Text("Nombre Completo del Titular") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mp_full_name_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = mpAccountInput,
                                onValueChange = { mpAccountInput = it },
                                label = { Text("CLABE (18 dígitos) o Teléfono / Correo") },
                                placeholder = { Text("012180015498263828") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mp_account_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        1 -> { // PayPal
                            Text(
                                text = "Retiro Internacional PayPal (USD)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = paypalEmail,
                                onValueChange = { paypalEmail = it },
                                label = { Text("Correo Electrónico de PayPal") },
                                placeholder = { Text("tu_cuenta@paypal.com") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("paypal_email_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        2 -> { // SPEI Direct Banco
                            Text(
                                text = "Transferencia Interbancaria SPEI 24/7",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = speiBeneficiary,
                                onValueChange = { speiBeneficiary = it },
                                label = { Text("Nombre del Beneficiario") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("spei_beneficiary_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = speiClabe,
                                onValueChange = { if (it.length <= 18) speiClabe = it },
                                label = { Text("CLABE Interbancaria (18 dígitos)") },
                                placeholder = { Text("012180015498263828") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("spei_clabe_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Withdrawal Amount Field
                    OutlinedTextField(
                        value = withdrawalAmountInput,
                        onValueChange = { withdrawalAmountInput = it },
                        label = { Text("Monto a Retirar (MXN)") },
                        leadingIcon = { Text("$", color = NeonCyan, fontWeight = FontWeight.Bold) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdrawal_amount_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmGold,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // One-Click Withdrawal Trigger Button with State Transitions (Pending -> Processing -> Success)
                    val isProcessing = withdrawalState == "Processing"
                    val isSuccess = withdrawalState == "Success"

                    Button(
                        onClick = {
                            val amount = withdrawalAmountInput.toDoubleOrNull() ?: 0.0
                            if (amount <= 0.0) {
                                withdrawalFeedback = "Ingresa un monto válido para retirar."
                                return@Button
                            }
                            if (amount > balanceMxn) {
                                withdrawalFeedback = "Saldo insuficiente para retirar $${amount} MXN."
                                return@Button
                            }

                            withdrawalFeedback = null
                            val method = when (selectedMethodTab) {
                                0 -> "MERCADO_PAGO"
                                1 -> "PAYPAL"
                                else -> "SPEI"
                            }
                            val dest = when (selectedMethodTab) {
                                0 -> mpAccountInput
                                1 -> paypalEmail
                                else -> speiClabe
                            }
                            val name = when (selectedMethodTab) {
                                0 -> mpFullName
                                1 -> "PayPal Account"
                                else -> speiBeneficiary
                            }

                            onExecuteWithdrawal(method, amount, dest, name)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("execute_withdrawal_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isSuccess) Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))
                                    else Brush.horizontalGradient(listOf(NeonCyan, NeonPurple, WarmGold)),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessing) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Procesando dispersión...",
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (isSuccess) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "¡Retiro Enviado con Éxito!",
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Retirar Instantáneamente ⚡",
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    withdrawalFeedback?.let { fb ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = fb,
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Payout Requests & Transactions History
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Historial de Retiros y Recompensas",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FuturisticDarkSurface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no hay transacciones registradas",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(transactions) { tx ->
                val isWithdrawal = tx.amountMxn < 0 || tx.description.contains("Retiro", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FuturisticDarkSurface)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("tx_item_${tx.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tx.sanitizedConcept,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isWithdrawal) "-$${String.format(java.util.Locale.US, "%.2f", tx.amountMxn)} MXN"
                                else "+$${String.format(java.util.Locale.US, "%.2f", tx.amountMxn)} MXN",
                                color = if (isWithdrawal) Color(0xFFFF5252) else Color(0xFF00E676),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = tx.status.name,
                                color = NeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
