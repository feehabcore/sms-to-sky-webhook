package com.payment.smsforwarder.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.payment.smsforwarder.data.model.DeliveryStatus
import com.payment.smsforwarder.data.model.NormalizedPaymentEvent

@Entity(
    tableName = "payment_events",
    indices = [
        Index(value = ["fingerprint"], unique = true),
        Index(value = ["transaction_id"]),
        Index(value = ["delivery_status"])
    ]
)
data class PaymentEventEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    val eventId: String,

    @ColumnInfo(name = "provider")
    val provider: String,

    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "currency")
    val currency: String,

    @ColumnInfo(name = "transaction_id")
    val transactionId: String,

    @ColumnInfo(name = "sender_identifier")
    val senderIdentifier: String,

    @ColumnInfo(name = "receiver_identifier")
    val receiverIdentifier: String? = null,

    @ColumnInfo(name = "reference")
    val reference: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: String,

    @ColumnInfo(name = "device_id")
    val deviceId: String,

    @ColumnInfo(name = "fingerprint")
    val fingerprint: String,

    @ColumnInfo(name = "raw_sms")
    val rawSms: String? = null,

    @ColumnInfo(name = "sms_sender")
    val smsSender: String? = null,

    @ColumnInfo(name = "delivery_status")
    val deliveryStatus: String = DeliveryStatus.PENDING.name,

    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int = 0,

    @ColumnInfo(name = "last_error")
    val lastError: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "delivered_at")
    val deliveredAt: Long? = null
) {
    fun toNormalizedEvent(): NormalizedPaymentEvent = NormalizedPaymentEvent(
        eventId = eventId,
        provider = provider,
        eventType = eventType,
        amount = amount,
        currency = currency,
        transactionId = transactionId,
        senderIdentifier = senderIdentifier,
        receiverIdentifier = receiverIdentifier,
        reference = reference,
        timestamp = timestamp,
        deviceId = deviceId,
        fingerprint = fingerprint,
        rawSms = rawSms
    )

    companion object {
        fun from(event: NormalizedPaymentEvent, smsSender: String?): PaymentEventEntity =
            PaymentEventEntity(
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
                fingerprint = event.fingerprint,
                rawSms = event.rawSms,
                smsSender = smsSender
            )
    }
}
