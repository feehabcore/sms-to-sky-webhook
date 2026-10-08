package com.payment.smsforwarder.data.model

enum class DeliveryStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
    DUPLICATE,
    IGNORED
}
