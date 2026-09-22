package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spei_accounts")
data class SpeiAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clabe: String,           // 18 digits
    val bankCode: String,        // First 3 digits e.g. "012", "638"
    val bankName: String,        // e.g. BBVA México, Nu México
    val beneficiaryName: String, // e.g. Carlos Mendoza Rivera
    val isVerified: Boolean = true,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
