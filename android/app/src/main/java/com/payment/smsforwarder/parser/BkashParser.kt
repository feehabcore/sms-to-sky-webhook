package com.payment.smsforwarder.parser

import com.payment.smsforwarder.data.model.ParserResult

class BkashParser : SmsParser {
    override val provider: String = "bkash"

    private val senders = setOf("BKASH", "bKash", "16247")

    override fun canHandle(sender: String, body: String): Boolean {
        val s = sender.trim().uppercase()
        if (senders.any { s.contains(it.uppercase()) }) return true
        val lower = body.lowercase()
        return lower.contains("bkash") && ParserHelpers.looksLikeIncomingPayment(body)
    }

    override fun parse(sender: String, body: String, deviceId: String): ParserResult {
        if (!ParserHelpers.looksLikeIncomingPayment(body)) {
            return ParserResult.Ignored("Not an incoming bKash payment SMS")
        }
        val amount = ParserHelpers.extractAmount(body)
            ?: return ParserResult.Unknown(body, sender, "Missing amount")
        val trx = ParserHelpers.extractTransactionId(body)
            ?: return ParserResult.Unknown(body, sender, "Missing TrxID")
        val from = ParserHelpers.extractSenderPhone(body) ?: "unknown"

        return ParserHelpers.buildSuccess(
            provider = provider,
            amount = amount,
            transactionId = trx,
            senderIdentifier = from,
            deviceId = deviceId,
            rawSms = body,
            reference = ParserHelpers.extractReference(body)
        )
    }
}
