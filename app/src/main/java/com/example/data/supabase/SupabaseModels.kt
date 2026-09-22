package com.example.data.supabase

data class SupabaseTodo(
    val id: String,
    val name: String,
    val isComplete: Boolean = false,
    val createdAt: String = ""
)

data class SupabaseHealthStatus(
    val isConnected: Boolean,
    val responseCode: Int,
    val latencyMs: Long,
    val projectUrl: String,
    val statusMessage: String
)

data class SupabaseSyncPayload(
    val userId: String,
    val balanceMxn: Double,
    val totalTransactions: Int,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
