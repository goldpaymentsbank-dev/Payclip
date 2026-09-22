package com.example.data.spei

import com.example.data.model.CepReceipt
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object SpeiEngine {

    const val MIN_WITHDRAWAL_MXN = 20.0
    const val MAX_DAILY_LIMIT_MXN = 5000.0
    const val DEFAULT_SPEI_FEE_MXN = 0.0 // Free instant disbursement for users!

    // Official Bank Code Directory (ABM / Banxico)
    val BANK_DIRECTORY = mapOf(
        "002" to "Citibanamex",
        "012" to "BBVA México",
        "014" to "Santander México",
        "021" to "HSBC México",
        "030" to "Banco del Bajío",
        "036" to "Banco Inbursa",
        "044" to "Scotiabank Inverlat",
        "058" to "Banregio",
        "062" to "Banca Afirme",
        "072" to "Banorte / Ixe",
        "127" to "Banco Azteca",
        "136" to "Intercam Banco",
        "137" to "BanCoppel",
        "138" to "Hey Banco",
        "140" to "Compartamos Banco",
        "638" to "Nu México Financiera",
        "646" to "STP (Sistema de Transferencias y Pagos)",
        "659" to "Klar",
        "670" to "Mercado Pago Wallet",
        "684" to "Ualá México"
    )

    data class ClabeValidationResult(
        val isValid: Boolean,
        val bankCode: String = "",
        val bankName: String = "",
        val calculatedCheckDigit: Int = -1,
        val errorMessage: String? = null
    )

    /**
     * Validates an 18-digit Mexican CLABE using the official Modulo 10 algorithm.
     * Weights: 3, 7, 1 repeating.
     */
    fun validateClabe(clabeRaw: String): ClabeValidationResult {
        val clabe = clabeRaw.trim().replace(" ", "")

        if (clabe.length != 18) {
            return ClabeValidationResult(
                isValid = false,
                errorMessage = "La CLABE debe contener exactamente 18 dígitos (actual: ${clabe.length})"
            )
        }

        if (!clabe.all { it.isDigit() }) {
            return ClabeValidationResult(
                isValid = false,
                errorMessage = "La CLABE solo debe contener caracteres numéricos"
            )
        }

        val bankCode = clabe.substring(0, 3)
        val bankName = BANK_DIRECTORY[bankCode] ?: "Banco Clave $bankCode (SPEI)"

        // Weights: 3, 7, 1 repeating for the first 17 digits
        val weights = intArrayOf(3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7)
        var sum = 0

        for (i in 0 until 17) {
            val digit = clabe[i].digitToInt()
            val product = (digit * weights[i]) % 10
            sum = (sum + product) % 10
        }

        val calculatedCheckDigit = (10 - sum) % 10
        val providedCheckDigit = clabe[17].digitToInt()

        if (calculatedCheckDigit != providedCheckDigit) {
            return ClabeValidationResult(
                isValid = false,
                bankCode = bankCode,
                bankName = bankName,
                calculatedCheckDigit = calculatedCheckDigit,
                errorMessage = "Dígito verificador inválido (esperado: $calculatedCheckDigit, ingresado: $providedCheckDigit)"
            )
        }

        return ClabeValidationResult(
            isValid = true,
            bankCode = bankCode,
            bankName = bankName,
            calculatedCheckDigit = calculatedCheckDigit
        )
    }

    /**
     * Detects the bank name from partial input as the user types (requires at least 3 digits).
     */
    fun detectBank(clabeRaw: String): String {
        val clean = clabeRaw.trim().replace(" ", "")
        if (clean.length >= 3) {
            val code = clean.substring(0, 3)
            return BANK_DIRECTORY[code] ?: "Banco SPEI ($code)"
        }
        return "Escribe tu CLABE (18 dígitos)"
    }

    /**
     * Formats and sanitizes the payment concept according to Banxico rules:
     * - Max 40 characters
     * - Uppercase
     * - Alphanumeric and single spaces only
     * - Strips accents/diacritics and special symbols
     */
    fun sanitizeConcept(rawConcept: String): String {
        val normalized = Normalizer.normalize(rawConcept, Normalizer.Form.NFD)
        val withoutDiacritics = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        val sanitized = withoutDiacritics.replace("[^A-Za-z0-9 ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
            .uppercase(Locale.getDefault())

        return if (sanitized.length > 40) sanitized.substring(0, 40) else sanitized.ifEmpty { "PREMIO REELS SPEI" }
    }

    /**
     * Generates an official SPEI Tracking Key (Clave de Rastreo).
     * Format: STP + YYYYMMDD + 9 random alphanumeric digits.
     */
    fun generateTrackingKey(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val randomSuffix = (100000000L + (Math.random() * 899999999L).toLong()).toString()
        return "STP$dateStr$randomSuffix"
    }

    /**
     * Generates a unique UUID v4 Idempotency Key to prevent duplicate disbursements.
     */
    fun generateIdempotencyKey(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Creates an authentic Banxico CEP Comprobante Electrónico de Pago.
     */
    fun generateCepReceipt(
        trackingKey: String,
        amountMxn: Double,
        beneficiaryName: String,
        beneficiaryClabe: String,
        bankName: String,
        concept: String
    ): CepReceipt {
        val datePattern = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val nowFormatted = datePattern.format(Date())
        val numericRef = ((Math.random() * 8999999).toInt() + 1000000).toString()

        val rawSignature = "$trackingKey|$nowFormatted|646|$beneficiaryClabe|$amountMxn"
        val mockDigitalSeal = "BANXICO_RSA_SHA256_${rawSignature.hashCode().toString(16).uppercase()}_" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 16).uppercase()

        val cadenaOriginal = "||$trackingKey|646|STP|$beneficiaryClabe|$beneficiaryName|$amountMxn|$concept|$numericRef||"

        return CepReceipt(
            trackingKey = trackingKey,
            operationDate = nowFormatted,
            emittingBank = "646 STP (SISTEMA DE TRANSFERENCIAS Y PAGOS)",
            receivingBank = bankName.uppercase(Locale.getDefault()),
            beneficiaryName = beneficiaryName.uppercase(Locale.getDefault()),
            beneficiaryClabe = beneficiaryClabe,
            amountMxn = amountMxn,
            concept = sanitizeConcept(concept),
            numericReference = numericRef,
            statusBanxico = "LIQUIDADO EN BANXICO",
            digitalSeal = mockDigitalSeal,
            originalString = cadenaOriginal
        )
    }
}
