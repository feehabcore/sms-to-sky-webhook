package com.payment.smsforwarder.parser

import com.payment.smsforwarder.data.model.ParserResult

/**
 * Fallback parser for payment-like SMS that did not match a known provider brand.
 */
class GenericParser : SmsParser {
    override val provider: String = "generic"

    override fun canHandle(sender: String, body: String): Boolean {
        return ParserHelpers.looksLikeIncomingPayment(body) &&
            ParserHelpers.extractAmount(body) != null &&
            ParserHelpers.extractTransactionId(body) != null
    }

    override fun parse(sender: String, body: String, deviceId: String): ParserResult {
        val amount = ParserHelpers.extractAmount(body)
            ?: return ParserResult.Unknown(body, sender, "Missing amount")
        val trx = ParserHelpers.extractTransactionId(body)
            ?: return ParserResult.Unknown(body, sender, "Missing transaction id")
        val from = ParserHelpers.extractSenderPhone(body) ?: sender.ifBlank { "unknown" }

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
