package com.payment.smsforwarder.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.payment.smsforwarder.data.model.NormalizedPaymentEvent
import com.payment.smsforwarder.data.prefs.SecureSettings
import com.payment.smsforwarder.util.HmacSigner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WebhookClient(private val settings: SecureSettings) {

    private val gson: Gson = GsonBuilder().serializeNulls().create()

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    private val api: WebhookApi = Retrofit.Builder()
        .baseUrl("https://placeholder.local/") // overridden by @Url
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(WebhookApi::class.java)

    sealed class Result {
        data class Success(val httpCode: Int, val body: String?) : Result()
        data class Failure(val httpCode: Int?, val message: String, val retryable: Boolean) : Result()
    }

    suspend fun deliver(event: NormalizedPaymentEvent): Result {
        val url = settings.webhookUrl
        if (url.isBlank()) {
            return Result.Failure(null, "Webhook URL not configured", retryable = false)
        }
        if (settings.apiToken.isBlank() || settings.hmacSecret.isBlank()) {
            return Result.Failure(null, "API token or HMAC secret missing", retryable = false)
        }

        val payload = WebhookPaymentPayload.from(event)
        val bodyJson = gson.toJson(payload)
        val timestamp = System.currentTimeMillis().toString()
        val signatureBase = "$timestamp.$bodyJson"
        val signature = HmacSigner.sign(signatureBase, settings.hmacSecret)

        return try {
            val response = api.postPayment(
                url = url,
                apiToken = settings.apiToken,
                signature = signature,
                deviceId = event.deviceId,
                timestamp = timestamp,
                payload = payload
            )
            val responseBody = response.body()?.string()
                ?: response.errorBody()?.string()
            if (response.isSuccessful) {
                Result.Success(response.code(), responseBody)
            } else {
                val retryable = response.code() in 408..599 && response.code() != 422
                Result.Failure(response.code(), "HTTP ${response.code()}: ${responseBody ?: "error"}", retryable)
            }
        } catch (e: Exception) {
            Result.Failure(null, e.message ?: "Network error", retryable = true)
        }
    }

    suspend fun testConnection(): Result {
        val testEvent = NormalizedPaymentEvent(
            eventId = "test-${System.currentTimeMillis()}",
            provider = "test",
            amount = 1.0,
            transactionId = "TEST${System.currentTimeMillis()}",
            senderIdentifier = "01700000000",
            timestamp = java.time.Instant.now().toString(),
            deviceId = "test-device",
            fingerprint = "test-fingerprint",
            rawSms = "TEST WEBHOOK PING"
        )
        return deliver(testEvent)
    }
}
