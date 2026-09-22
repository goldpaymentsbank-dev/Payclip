package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SpeiAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeiAccountDao {

    @Query("SELECT * FROM spei_accounts ORDER BY isDefault DESC, createdAt DESC")
    fun getAllAccounts(): Flow<List<SpeiAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: SpeiAccountEntity): Long

    @Query("SELECT * FROM spei_accounts WHERE clabe = :clabe LIMIT 1")
    suspend fun getAccountByClabe(clabe: String): SpeiAccountEntity?

    @Query("DELETE FROM spei_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)
}
