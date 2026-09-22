package com.example.data.repository

import com.example.data.local.PayoutRequestDao
import com.example.data.local.SpeiAccountDao
import com.example.data.local.UserDao
import com.example.data.local.UserPaymentMethodDao
import com.example.data.local.WalletDao
import com.example.data.model.CepReceipt
import com.example.data.model.PayoutRequestEntity
import com.example.data.model.SpeiAccountEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.UserPaymentMethodEntity
import com.example.data.model.WalletTransactionEntity
import com.example.data.spei.SpeiEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.UUID

class WalletRepository(
    private val walletDao: WalletDao,
    private val speiAccountDao: SpeiAccountDao,
    private val userDao: UserDao? = null,
    private val paymentMethodDao: UserPaymentMethodDao? = null,
    private val payoutRequestDao: PayoutRequestDao? = null
) {

    val allTransactions: Flow<List<WalletTransactionEntity>> = walletDao.getAllTransactions()
    val savedAccounts: Flow<List<SpeiAccountEntity>> = speiAccountDao.getAllAccounts()
    val currentUserFlow: Flow<UserEntity?>? = userDao?.getCurrentUserFlow()
    val paymentMethodsFlow: Flow<List<UserPaymentMethodEntity>>? = paymentMethodDao?.getPaymentMethodsFlow("usr_vip_001")
    val payoutRequestsFlow: Flow<List<PayoutRequestEntity>>? = payoutRequestDao?.getPayoutRequestsFlow("usr_vip_001")

    // Calculate live net balance
    val balanceFlow: Flow<Double> = combine(
        walletDao.getTotalIncomeFlow(),
        walletDao.getTotalExpenseFlow()
    ) { income, expense ->
        val inc = income ?: 0.0
        val exp = expense ?: 0.0
        (inc - exp).coerceAtLeast(0.0)
    }

    // Calculate today's withdrawn amount
    fun getTodayWithdrawnFlow(): Flow<Double> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return combine(walletDao.getTodayWithdrawnFlow(cal.timeInMillis)) { withdrawn ->
            withdrawn[0] ?: 0.0
        }
    }

    suspend fun addWatchReward(amount: Double, videoTitle: String): Long {
        val entity = WalletTransactionEntity(
            amountMxn = amount,
            type = TransactionType.WATCH_REWARD,
            description = "Watch-to-Earn: $videoTitle",
            timestamp = System.currentTimeMillis(),
            status = TransactionStatus.COMPLETED,
            sanitizedConcept = SpeiEngine.sanitizeConcept("WATCH TO EARN $videoTitle")
        )
        val coins = (amount * 100).toLong()
        userDao?.creditRewards("usr_vip_001", coins, amount)
        return walletDao.insertTransaction(entity)
    }

    suspend fun addTriviaReward(amount: Double, sponsorName: String): Long {
        val entity = WalletTransactionEntity(
            amountMxn = amount,
            type = TransactionType.TRIVIA_BONUS,
            description = "Trivia Patrocinada: $sponsorName",
            timestamp = System.currentTimeMillis(),
            status = TransactionStatus.COMPLETED,
            sanitizedConcept = SpeiEngine.sanitizeConcept("PREMIO TRIVIA $sponsorName")
        )
        val coins = (amount * 100).toLong()
        userDao?.creditRewards("usr_vip_001", coins, amount)
        return walletDao.insertTransaction(entity)
    }

    suspend fun addChallengeWin(amount: Double, challengeTitle: String): Long {
        val entity = WalletTransactionEntity(
            amountMxn = amount,
            type = TransactionType.CHALLENGE_WIN,
            description = "Premio de Campaña: $challengeTitle",
            timestamp = System.currentTimeMillis(),
            status = TransactionStatus.COMPLETED,
            sanitizedConcept = SpeiEngine.sanitizeConcept("PREMIO RETO $challengeTitle")
        )
        val coins = (amount * 100).toLong()
        userDao?.creditRewards("usr_vip_001", coins, amount)
        return walletDao.insertTransaction(entity)
    }

    suspend fun sendLiveGift(amount: Double, giftName: String, creatorName: String): Long {
        val entity = WalletTransactionEntity(
            amountMxn = amount,
            type = TransactionType.LIVE_GIFT_SENT,
            description = "Regalo $giftName enviado a $creatorName",
            timestamp = System.currentTimeMillis(),
            status = TransactionStatus.COMPLETED,
            sanitizedConcept = SpeiEngine.sanitizeConcept("REGALO EN VIVO $giftName")
        )
        val coins = (amount * 100).toLong()
        userDao?.debitBalance("usr_vip_001", coins, amount)
        return walletDao.insertTransaction(entity)
    }

    suspend fun claimWelcomeBonus(): Boolean {
        val user = userDao?.getCurrentUser()
        if (user != null && !user.hasReceivedWelcomeBonus) {
            val bonusMxn = 150.0
            val bonusCoins = 15000L
            userDao.updateUser(user.copy(
                hasReceivedWelcomeBonus = true,
                coinBalance = user.coinBalance + bonusCoins,
                mxnBalance = user.mxnBalance + bonusMxn,
                usdBalance = user.usdBalance + (bonusMxn / 18.0)
            ))
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    amountMxn = bonusMxn,
                    type = TransactionType.WATCH_REWARD,
                    description = "🎉 ¡Bono de $150 MXN asignado por ser de los primeros 1,000,000 de usuarios!",
                    timestamp = System.currentTimeMillis(),
                    status = TransactionStatus.COMPLETED,
                    sanitizedConcept = "BONO PRIMEROS 1M USUARIOS"
                )
            )
            return true
        }
        return false
    }

    sealed class WithdrawalResult {
        data class Success(val transaction: WalletTransactionEntity, val cep: CepReceipt) : WithdrawalResult()
        data class Error(val message: String) : WithdrawalResult()
    }

    /**
     * Executes instant withdrawal via SPEI / Mercado Pago / PayPal.
     */
    suspend fun processWithdrawalWithMethod(
        method: String, // "MERCADO_PAGO", "PAYPAL", "SPEI"
        amountMxn: Double,
        destination: String,
        holderName: String,
        concept: String,
        currentBalance: Double,
        todayWithdrawn: Double
    ): WithdrawalResult {
        if (amountMxn < SpeiEngine.MIN_WITHDRAWAL_MXN) {
            return WithdrawalResult.Error("El monto mínimo de retiro es de $${SpeiEngine.MIN_WITHDRAWAL_MXN} MXN")
        }

        if (amountMxn > currentBalance) {
            return WithdrawalResult.Error("Saldo insuficiente. Tu saldo disponible es de $${String.format("%.2f", currentBalance)} MXN")
        }

        if (todayWithdrawn + amountMxn > SpeiEngine.MAX_DAILY_LIMIT_MXN) {
            val remainingLimit = (SpeiEngine.MAX_DAILY_LIMIT_MXN - todayWithdrawn).coerceAtLeast(0.0)
            return WithdrawalResult.Error("Has superado el límite diario de retiro. Límite restante hoy: $${String.format("%.2f", remainingLimit)} MXN")
        }

        val trackingKey = SpeiEngine.generateTrackingKey()
        val idempotencyKey = SpeiEngine.generateIdempotencyKey()
        val sanitizedConcept = SpeiEngine.sanitizeConcept(concept.ifEmpty { "RETIRO REELS $method" })

        val providerName = when (method) {
            "MERCADO_PAGO" -> "Mercado Pago Wallet"
            "PAYPAL" -> "PayPal Inc."
            else -> "SPEI Directo Banxico"
        }

        // Record payout request
        val requestId = "PO-" + UUID.randomUUID().toString().take(8).uppercase()
        payoutRequestDao?.insertPayoutRequest(
            PayoutRequestEntity(
                id = requestId,
                userId = "usr_vip_001",
                amountMxn = amountMxn,
                amountUsd = amountMxn / 18.0,
                method = method,
                destination = destination,
                status = "Success",
                trackingKey = trackingKey
            )
        )

        val transaction = WalletTransactionEntity(
            amountMxn = amountMxn,
            type = TransactionType.SPEI_WITHDRAWAL,
            description = "Retiro $method a $destination ($providerName)",
            timestamp = System.currentTimeMillis(),
            status = TransactionStatus.COMPLETED,
            idempotencyKey = idempotencyKey,
            trackingKey = trackingKey,
            targetClabe = destination,
            bankName = providerName,
            feeMxn = 0.0,
            sanitizedConcept = sanitizedConcept
        )

        val insertedId = walletDao.insertTransaction(transaction)
        val coins = (amountMxn * 100).toLong()
        userDao?.debitBalance("usr_vip_001", coins, amountMxn)

        val cep = SpeiEngine.generateCepReceipt(
            trackingKey = trackingKey,
            amountMxn = amountMxn,
            beneficiaryName = holderName.ifEmpty { "BENEFICIARIO RETIRO" },
            beneficiaryClabe = destination,
            bankName = providerName,
            concept = sanitizedConcept
        )

        return WithdrawalResult.Success(transaction.copy(id = insertedId), cep)
    }

    suspend fun processSpeiWithdrawal(
        amountMxn: Double,
        targetClabe: String,
        beneficiaryName: String,
        concept: String,
        saveAccount: Boolean,
        currentBalance: Double,
        todayWithdrawn: Double
    ): WithdrawalResult {
        if (saveAccount) {
            val clabeRes = SpeiEngine.validateClabe(targetClabe)
            speiAccountDao.insertAccount(
                SpeiAccountEntity(
                    clabe = targetClabe,
                    beneficiaryName = beneficiaryName,
                    bankName = if (clabeRes.bankName.isNotEmpty()) clabeRes.bankName else "BANCO",
                    bankCode = if (clabeRes.bankCode.isNotEmpty()) clabeRes.bankCode else "000",
                    isVerified = true,
                    isDefault = true
                )
            )
        }
        return processWithdrawalWithMethod(
            method = "SPEI",
            amountMxn = amountMxn,
            destination = targetClabe,
            holderName = beneficiaryName,
            concept = concept,
            currentBalance = currentBalance,
            todayWithdrawn = todayWithdrawn
        )
    }

    suspend fun addPaymentMethod(method: UserPaymentMethodEntity) {
        paymentMethodDao?.insertMethod(method)
    }

    suspend fun deletePaymentMethod(id: Long) {
        paymentMethodDao?.deleteMethod(id)
    }
}
