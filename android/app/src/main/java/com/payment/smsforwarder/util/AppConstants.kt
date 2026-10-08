package com.payment.smsforwarder.util

object AppConstants {
    const val DEFAULT_CURRENCY = "BDT"
    const val EVENT_TYPE_PAYMENT_RECEIVED = "payment_received"

    val SUPPORTED_PROVIDERS = listOf(
        "bkash", "nagad", "rocket", "upay", "generic", "test"
    )

    /** Retry delays in seconds — mirrors shared/src/constants.ts */
    val RETRY_DELAYS_SECONDS = listOf(10L, 30L, 60L, 300L, 900L, 1800L)

    const val MAX_RETRY_ATTEMPTS = 6

    const val HEADER_API_TOKEN = "X-Api-Token"
    const val HEADER_HMAC_SIGNATURE = "X-Signature"
    const val HEADER_DEVICE_ID = "X-Device-Id"
    const val HEADER_TIMESTAMP = "X-Timestamp"

    const val NOTIFICATION_CHANNEL_ID = "gateway_service"
    const val NOTIFICATION_ID_FOREGROUND = 1001

    const val WORK_TAG_WEBHOOK = "webhook_delivery"
    const val WORK_NAME_PREFIX = "deliver_"
}
