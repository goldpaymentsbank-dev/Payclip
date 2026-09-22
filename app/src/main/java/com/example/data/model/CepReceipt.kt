package com.example.data.model

data class CepReceipt(
    val trackingKey: String,          // Clave de Rastreo (e.g. STP2026092288392019)
    val operationDate: String,        // e.g. "22/09/2026 14:32:05"
    val emittingBank: String,         // "646 STP (Sistema de Transferencias y Pagos)"
    val receivingBank: String,        // e.g. "012 BBVA MEXICO"
    val beneficiaryName: String,
    val beneficiaryClabe: String,     // 18 digits
    val senderName: String = "SPEI REELS TECH S.A.P.I. DE C.V.",
    val senderClabe: String = "646180123049182390",
    val amountMxn: Double,
    val concept: String,              // Sanitized, max 40 chars
    val numericReference: String,     // 7 digits
    val statusBanxico: String = "LIQUIDADO EN BANXICO",
    val digitalSeal: String,          // Cryptographic seal simulation
    val originalString: String
)
