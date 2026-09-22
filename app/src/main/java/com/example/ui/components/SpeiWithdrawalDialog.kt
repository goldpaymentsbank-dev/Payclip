package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeiAccountEntity
import com.example.data.spei.SpeiEngine
import com.example.ui.theme.BanxicoGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SpeiGold
import com.example.ui.theme.SpeiGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeiWithdrawalDialog(
    currentBalance: Double,
    todayWithdrawn: Double,
    savedAccounts: List<SpeiAccountEntity>,
    processingState: String?,
    onExecuteWithdrawal: (Double, String, String, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Pre-select default account if present
    val defaultAccount = savedAccounts.firstOrNull { it.isDefault } ?: savedAccounts.firstOrNull()

    var clabeInput by remember { mutableStateOf(defaultAccount?.clabe ?: "") }
    var beneficiaryInput by remember { mutableStateOf(defaultAccount?.beneficiaryName ?: "Carlos Mendoza Rivera") }
    var amountInput by remember { mutableStateOf(if (currentBalance >= 50.0) "50" else String.format("%.0f", currentBalance)) }
    var conceptInput by remember { mutableStateOf("PREMIO RETO REELS 102") }
    var saveAccount by remember { mutableStateOf(true) }
    var localError by remember { mutableStateOf<String?>(null) }

    // Live validation
    val clabeValidation by remember(clabeInput) {
        derivedStateOf { SpeiEngine.validateClabe(clabeInput) }
    }

    val detectedBankName by remember(clabeInput) {
        derivedStateOf { SpeiEngine.detectBank(clabeInput) }
    }

    val sanitizedConceptPreview by remember(conceptInput) {
        derivedStateOf { SpeiEngine.sanitizeConcept(conceptInput) }
    }

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0

    ModalBottomSheet(
        onDismissRequest = {
            if (processingState == null) onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color(0xFF0F131A),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("spei_withdrawal_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpeiGreenPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "SPEI Instantáneo",
                        tint = SpeiGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DISPERSIÓN INSTANTÁNEA SPEI",
                        color = SpeiGreenPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Retirar a tu Banco en Segundos",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (processingState == null) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Balance & Anti-Fraud Limit Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF161D28))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saldo acumulado disponible:",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$${String.format("%.2f", currentBalance)} MXN",
                            color = SpeiGreenPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Límite Diario",
                                tint = SpeiGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Límite diario ($${String.format("%.0f", SpeiEngine.MAX_DAILY_LIMIT_MXN)}):",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "Retirado hoy: $${String.format("%.2f", todayWithdrawn)} MXN",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Processing Pipeline Display
            if (processingState != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D2517))
                        .border(1.5.dp, SpeiGreenPrimary, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = SpeiGreenPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = processingState,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Transferencia 24/7 vía Banco de México (Banxico) / STP",
                            color = Color(0xFFA7F3D0),
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                // Saved Accounts Fast Selector
                if (savedAccounts.isNotEmpty()) {
                    Text(
                        text = "Cuentas registradas:",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        savedAccounts.forEach { acc ->
                            val isSelected = clabeInput == acc.clabe
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SpeiGreenPrimary.copy(alpha = 0.2f) else Color(0xFF1E2634))
                                    .border(
                                        1.dp,
                                        if (isSelected) SpeiGreenPrimary else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        clabeInput = acc.clabe
                                        beneficiaryInput = acc.beneficiaryName
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = acc.bankName,
                                        tint = if (isSelected) SpeiGreenPrimary else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${acc.bankName} (..${acc.clabe.takeLast(4)})",
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 18-digit CLABE Input
                OutlinedTextField(
                    value = clabeInput,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 18) {
                            clabeInput = digits
                            localError = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_clabe"),
                    label = { Text("CLABE Interbancaria (18 dígitos)") },
                    placeholder = { Text("012180015498263828") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (clabeValidation.isValid) SpeiGreenPrimary else Color(0xFF00E676),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = SpeiGreenPrimary,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        if (clabeInput.length == 18) {
                            if (clabeValidation.isValid) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "CLABE Válida (Módulo 10)",
                                    tint = SpeiGreenPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "CLABE Inválida",
                                    tint = ErrorRed
                                )
                            }
                        }
                    }
                )

                // Bank Identification & Check-Digit Feedback Banner
                if (clabeInput.length >= 3) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF162130))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Banco",
                            tint = SpeiGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Banco receptor: ",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = detectedBankName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${clabeInput.length}/18",
                            color = if (clabeInput.length == 18) SpeiGreenPrimary else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (clabeInput.length == 18 && !clabeValidation.isValid) {
                    Text(
                        text = clabeValidation.errorMessage ?: "Dígito verificador inválido",
                        color = ErrorRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Beneficiary Name
                OutlinedTextField(
                    value = beneficiaryInput,
                    onValueChange = { beneficiaryInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_beneficiary"),
                    label = { Text("Nombre del Titular") },
                    placeholder = { Text("Ej. Juan Pérez García") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpeiGreenPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = SpeiGreenPrimary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Selection with Quick Chips
                Text(
                    text = "Monto a retirar en MXN:",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(20.0, 50.0, 100.0, 200.0)
                    presets.forEach { preset ->
                        val isSelected = parsedAmount == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { amountInput = preset.toInt().toString() },
                            label = { Text("$$preset") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpeiGreenPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E2634),
                                labelColor = Color.White
                            )
                        )
                    }
                    FilterChip(
                        selected = parsedAmount == currentBalance && currentBalance > 0,
                        onClick = { amountInput = String.format("%.0f", currentBalance) },
                        label = { Text("Todo") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpeiGold,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E2634),
                            labelColor = SpeiGold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        localError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_amount"),
                    label = { Text("Monto personalizado ($ MXN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpeiGreenPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = SpeiGreenPrimary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        Text(
                            text = "MXN",
                            color = SpeiGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Concept & Live Sanitizer
                OutlinedTextField(
                    value = conceptInput,
                    onValueChange = { conceptInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_concept"),
                    label = { Text("Concepto de Pago (SPEI Sanitizado)") },
                    placeholder = { Text("Ej. PREMIO RETO VIDEO 102") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpeiGreenPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = SpeiGreenPrimary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Text(
                    text = "Banxico SPEI: \"$sanitizedConceptPreview\" (${sanitizedConceptPreview.length}/40)",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Anti-Fraud & Identity Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141F1A))
                        .border(1.dp, BanxicoGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Anti-Fraude",
                        tint = BanxicoGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DISPOSITIVO Y NÚMERO (OTP) VERIFICADO",
                            color = BanxicoGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Protección de idempotencia activa contra cobros dobles.",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }

                // Error Banner if present
                if (localError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = localError!!,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Action Button
                Button(
                    onClick = {
                        if (clabeInput.length != 18) {
                            localError = "Ingresa los 18 dígitos completos de tu CLABE"
                            return@Button
                        }
                        if (!clabeValidation.isValid) {
                            localError = clabeValidation.errorMessage ?: "Dígito verificador incorrecto"
                            return@Button
                        }
                        if (parsedAmount < SpeiEngine.MIN_WITHDRAWAL_MXN) {
                            localError = "El retiro mínimo es de $${SpeiEngine.MIN_WITHDRAWAL_MXN} MXN"
                            return@Button
                        }
                        if (parsedAmount > currentBalance) {
                            localError = "Saldo insuficiente. Tu saldo es de $${String.format("%.2f", currentBalance)} MXN"
                            return@Button
                        }

                        onExecuteWithdrawal(
                            parsedAmount,
                            clabeInput,
                            beneficiaryInput,
                            sanitizedConceptPreview,
                            saveAccount
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_spei_withdrawal_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SpeiGreenPrimary),
                    enabled = parsedAmount > 0 && clabeInput.length == 18
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transferir $${String.format("%.2f", parsedAmount)} MXN por SPEI",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Seguro",
                        tint = Color.Gray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Conexión encriptada directa con STP y Banco de México",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
