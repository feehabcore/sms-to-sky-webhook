package com.payment.smsforwarder.data.model

import com.google.gson.annotations.SerializedName

data class NormalizedPaymentEvent(
    @SerializedName("event_id")
    val eventId: String,

    @SerializedName("provider")
    val provider: String,

    @SerializedName("event_type")
    val eventType: String = "payment_received",

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String = "BDT",

    @SerializedName("transaction_id")
    val transactionId: String,

    @SerializedName("sender_identifier")
    val senderIdentifier: String,

    @SerializedName("receiver_identifier")
    val receiverIdentifier: String? = null,

    @SerializedName("reference")
    val reference: String? = null,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("fingerprint")
    val fingerprint: String,

    @SerializedName("raw_sms")
    val rawSms: String? = null
)

sealed class ParserResult {
    data class Success(val event: NormalizedPaymentEvent) : ParserResult()
    data class Ignored(val reason: String) : ParserResult()
    data class Unknown(val rawSms: String, val sender: String, val reason: String) : ParserResult()
}
