package com.payment.smsforwarder.util

import java.security.MessageDigest

object FingerprintUtil {

    /**
     * Stable fingerprint for deduplication:
     * SHA-256(provider|transactionId|amount|senderIdentifier)
     */
    fun compute(
        provider: String,
        transactionId: String,
        amount: Double,
        senderIdentifier: String
    ): String {
        val raw = listOf(
            provider.lowercase().trim(),
            transactionId.trim(),
            "%.2f".format(amount),
            senderIdentifier.trim()
        ).joinToString("|")
        return sha256(raw)
    }

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
