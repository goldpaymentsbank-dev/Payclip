package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    WATCH_REWARD,        // Watch-to-Earn daily visual rewards
    TRIVIA_BONUS,        // Interactive sponsored trivia bonus
    CHALLENGE_WIN,       // UGC Brand challenge payout (#RetoMarcaX)
    LIVE_GIFT_RECEIVED,  // Creator monetization from live viewer gifts
    LIVE_GIFT_SENT,      // User sending gift to creator
    SPEI_WITHDRAWAL      // Instant payout to Mexican CLABE via SPEI
}

enum class TransactionStatus {
    COMPLETED,
    PROCESSING,
    FAILED
}

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMxn: Double,
    val type: TransactionType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val idempotencyKey: String = "",
    val trackingKey: String = "",       // Clave de Rastreo SPEI (e.g. STP20260922...)
    val targetClabe: String = "",       // 18-digit CLABE
    val bankName: String = "",          // e.g. BBVA México, Nu México
    val feeMxn: Double = 0.0,           // SPEI dispersion fee (if applicable)
    val sanitizedConcept: String = ""   // Concepto SPEI (max 40 chars uppercase)
)
