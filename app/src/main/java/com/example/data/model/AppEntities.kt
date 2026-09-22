package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = "usr_vip_001",
    val authProvider: String = "email", // "email", "facebook", "tiktok", "guest"
    val email: String = "creator@speireels.mx",
    val username: String = "Alex_ReelsMX",
    val coinBalance: Long = 15000L, // 15,000 coins = $150.00 MXN
    val usdBalance: Double = 8.50,
    val mxnBalance: Double = 150.0,
    val hasReceivedWelcomeBonus: Boolean = true,
    val userRankNumber: Int = 48291, // E.g. #48,291 of the first 1,000,000 users!
    val avatarUrl: String = ""
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val creatorId: String,
    val creatorName: String,
    val creatorHandle: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val rewardCoins: Int = 50, // Coins per watch cycle
    val category: String, // "Fintech", "Gaming", "Tech", "Lifestyle", "Comedy", "Crypto"
    val likesCount: Int = 1240,
    val commentsCount: Int = 89,
    val sharesCount: Int = 45,
    val isLiked: Boolean = false,
    val sponsorBrand: String? = null,
    val soundTitle: String = "Original Sound - SPEI Beats"
)

@Entity(tableName = "video_views")
data class VideoViewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: String,
    val userId: String,
    val watchDurationSeconds: Int,
    val coinsClaimed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_payment_methods")
data class UserPaymentMethodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val type: String, // "MERCADO_PAGO", "PAYPAL", "SPEI"
    val fullName: String,
    val accountIdentifier: String, // 18-digit CLABE, Phone/Email (Mercado Pago), or PayPal email
    val isDefault: Boolean = false,
    val bankOrProvider: String = ""
)

@Entity(tableName = "payout_requests")
data class PayoutRequestEntity(
    @PrimaryKey
    val id: String, // e.g. "PO-84920"
    val userId: String,
    val amountMxn: Double,
    val amountUsd: Double,
    val method: String, // "MERCADO_PAGO", "PAYPAL", "SPEI"
    val destination: String,
    val status: String, // "Pending", "Processing", "Success"
    val trackingKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
