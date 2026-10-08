package com.payment.smsforwarder.parser

import com.payment.smsforwarder.data.model.NormalizedPaymentEvent
import com.payment.smsforwarder.data.model.ParserResult
import com.payment.smsforwarder.util.AppConstants
import com.payment.smsforwarder.util.FingerprintUtil
import java.time.Instant
import java.util.UUID
import java.util.regex.Pattern

internal object ParserHelpers {

    private val AMOUNT_PATTERNS = listOf(
        Pattern.compile(
            """(?i)(?:tk|bdt|৳)\s*([\d,]+(?:\.\d{1,2})?)""",
            Pattern.CASE_INSENSITIVE
        ),
        Pattern.compile(
            """(?i)(?:received|received money|cash in|payment of)\s+(?:tk|bdt|৳)?\s*([\d,]+(?:\.\d{1,2})?)""",
            Pattern.CASE_INSENSITIVE
        )
    )

    private val TRX_PATTERNS = listOf(
        Pattern.compile(
            """(?i)(?:trxid|trx\s*id|transaction\s*id|txn\s*id|trans\s*id)\s*[:#]?\s*([A-Za-z0-9]+)"""
        ),
        Pattern.compile("""(?i)\bTrxID\s+([A-Za-z0-9]+)""")
    )

    private val SENDER_PHONE_PATTERNS = listOf(
        Pattern.compile(
            """(?i)(?:from|sender)\s+(?:a\/c\s*)?(\+?8801\d{9}|01\d{9}|\d{11,13})"""
        ),
        Pattern.compile("""(?i)(?:from)\s+(\+?8801\d{9}|01\d{9})""")
    )

    private val REF_PATTERNS = listOf(
        Pattern.compile("""(?i)(?:ref|reference|nar|narration)\s*[:#]?\s*([A-Za-z0-9\-_/ ]{2,40})""")
    )

    fun extractAmount(body: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val raw = matcher.group(1)?.replace(",", "") ?: continue
                return raw.toDoubleOrNull()
            }
        }
        return null
    }

    fun extractTransactionId(body: String): String? {
        for (pattern in TRX_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                return matcher.group(1)?.trim()
            }
        }
        return null
    }

    fun extractSenderPhone(body: String): String? {
        for (pattern in SENDER_PHONE_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                return normalizePhone(matcher.group(1))
            }
        }
        return null
    }

    fun extractReference(body: String): String? {
        for (pattern in REF_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                return matcher.group(1)?.trim()
            }
        }
        return null
    }

    fun normalizePhone(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        var digits = raw.filter { it.isDigit() || it == '+' }
        if (digits.startsWith("+880")) digits = "0" + digits.removePrefix("+880")
        if (digits.startsWith("880") && digits.length == 13) digits = "0" + digits.removePrefix("880")
        return digits
    }

    fun buildSuccess(
        provider: String,
        amount: Double,
        transactionId: String,
        senderIdentifier: String,
        deviceId: String,
        rawSms: String,
        receiverIdentifier: String? = null,
        reference: String? = null
    ): ParserResult.Success {
        val fingerprint = FingerprintUtil.compute(
            provider, transactionId, amount, senderIdentifier
        )
        val event = NormalizedPaymentEvent(
            eventId = UUID.randomUUID().toString(),
            provider = provider,
            eventType = AppConstants.EVENT_TYPE_PAYMENT_RECEIVED,
            amount = amount,
            currency = AppConstants.DEFAULT_CURRENCY,
            transactionId = transactionId,
            senderIdentifier = senderIdentifier,
            receiverIdentifier = receiverIdentifier,
            reference = reference,
            timestamp = Instant.now().toString(),
            deviceId = deviceId,
            fingerprint = fingerprint,
            rawSms = rawSms
        )
        return ParserResult.Success(event)
    }

    fun looksLikeIncomingPayment(body: String): Boolean {
        val lower = body.lowercase()
        val positive = listOf(
            "received", "cash in", "you have received", "payment received",
            "money received", "credited", "deposit"
        )
        val negative = listOf(
            "sent", "cash out", "payment to", "withdraw", "purchase",
            "failed", "unsuccessful", "otp", "pin", "login"
        )
        if (negative.any { lower.contains(it) } && !positive.any { lower.contains(it) }) {
            return false
        }
        return positive.any { lower.contains(it) } ||
            (lower.contains("tk") && lower.contains("trx"))
    }
}
