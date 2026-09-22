package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): WalletTransactionEntity?

    @Query("SELECT * FROM wallet_transactions WHERE trackingKey = :trackingKey LIMIT 1")
    suspend fun getTransactionByTrackingKey(trackingKey: String): WalletTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity): Long

    @Query("SELECT SUM(amountMxn) FROM wallet_transactions WHERE type != 'SPEI_WITHDRAWAL' AND type != 'LIVE_GIFT_SENT' AND status = 'COMPLETED'")
    fun getTotalIncomeFlow(): Flow<Double?>

    @Query("SELECT SUM(amountMxn) FROM wallet_transactions WHERE (type = 'SPEI_WITHDRAWAL' OR type = 'LIVE_GIFT_SENT') AND status = 'COMPLETED'")
    fun getTotalExpenseFlow(): Flow<Double?>

    @Query("SELECT SUM(amountMxn) FROM wallet_transactions WHERE type = 'SPEI_WITHDRAWAL' AND status = 'COMPLETED' AND timestamp >= :startOfDayTimestamp")
    fun getTodayWithdrawnFlow(startOfDayTimestamp: Long): Flow<Double?>

    @Query("DELETE FROM wallet_transactions")
    suspend fun clearAll()
}
