package com.payment.smsforwarder.network

import com.google.gson.annotations.SerializedName
import com.payment.smsforwarder.data.model.NormalizedPaymentEvent

data class WebhookPaymentPayload(
    @SerializedName("event_id")
    val eventId: String,
    @SerializedName("provider")
    val provider: String,
    @SerializedName("event_type")
    val eventType: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("currency")
    val currency: String,
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
    val fingerprint: String? = null
) {
    companion object {
        fun from(event: NormalizedPaymentEvent): WebhookPaymentPayload =
            WebhookPaymentPayload(
                eventId = event.eventId,
                provider = event.provider,
                eventType = event.eventType,
                amount = event.amount,
                currency = event.currency,
                transactionId = event.transactionId,
                senderIdentifier = event.senderIdentifier,
                receiverIdentifier = event.receiverIdentifier,
                reference = event.reference,
                timestamp = event.timestamp,
                deviceId = event.deviceId,
                fingerprint = event.fingerprint
            )
    }
}

data class WebhookResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null
)
