package com.payment.smsforwarder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.data.model.DeliveryStatus
import com.payment.smsforwarder.network.WebhookClient
import com.payment.smsforwarder.util.AppConstants

class WebhookDeliveryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as GatewayApp
        val processQueue = inputData.getBoolean(KEY_PROCESS_QUEUE, false)
        val eventId = inputData.getString(KEY_EVENT_ID)

        return when {
            processQueue -> processQueue(app)
            !eventId.isNullOrBlank() -> deliverOne(app, eventId)
            else -> Result.failure()
        }
    }

    private suspend fun processQueue(app: GatewayApp): Result {
        val pending = app.repository.getPendingOrFailed()
        var anyRetry = false
        for (entity in pending) {
            val outcome = deliverEntity(app, entity.eventId)
            if (outcome == Outcome.RETRY) anyRetry = true
        }
        return if (anyRetry) Result.retry() else Result.success()
    }

    private suspend fun deliverOne(app: GatewayApp, eventId: String): Result {
        return when (deliverEntity(app, eventId)) {
            Outcome.SUCCESS, Outcome.GIVE_UP -> Result.success()
            Outcome.RETRY -> Result.retry()
            Outcome.MISSING -> Result.failure()
        }
    }

    private suspend fun deliverEntity(app: GatewayApp, eventId: String): Outcome {
        val entity = app.repository.getById(eventId) ?: return Outcome.MISSING
        if (entity.deliveryStatus == DeliveryStatus.SENT.name) return Outcome.SUCCESS

        if (!app.secureSettings.isConfigured()) {
            app.repository.markFailed(eventId, entity.attemptCount, "Gateway not configured")
            return Outcome.GIVE_UP
        }

        val nextAttempt = entity.attemptCount + 1
        app.repository.markSending(eventId, nextAttempt)

        return when (val result = app.webhookClient.deliver(entity.toNormalizedEvent())) {
            is WebhookClient.Result.Success -> {
                app.repository.markSent(eventId, nextAttempt)
                Outcome.SUCCESS
            }
            is WebhookClient.Result.Failure -> {
                app.repository.markFailed(eventId, nextAttempt, result.message)
                if (!result.retryable || nextAttempt >= AppConstants.MAX_RETRY_ATTEMPTS) {
                    Outcome.GIVE_UP
                } else {
                    val delayIdx = (nextAttempt - 1).coerceIn(0, AppConstants.RETRY_DELAYS_SECONDS.lastIndex)
                    val delay = AppConstants.RETRY_DELAYS_SECONDS[delayIdx]
                    DeliveryScheduler.enqueueEvent(applicationContext, eventId, delay)
                    Outcome.RETRY
                }
            }
        }
    }

    private enum class Outcome { SUCCESS, RETRY, GIVE_UP, MISSING }

    companion object {
        const val KEY_EVENT_ID = "event_id"
        const val KEY_PROCESS_QUEUE = "process_queue"
    }
}
