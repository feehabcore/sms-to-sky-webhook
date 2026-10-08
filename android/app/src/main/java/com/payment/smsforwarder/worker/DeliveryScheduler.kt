package com.payment.smsforwarder.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.payment.smsforwarder.util.AppConstants
import java.util.concurrent.TimeUnit

object DeliveryScheduler {

    private const val PERIODIC_RETRY_NAME = "periodic_webhook_retry"

    fun enqueueEvent(context: Context, eventId: String, delaySeconds: Long = 0L) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<WebhookDeliveryWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(WebhookDeliveryWorker.KEY_EVENT_ID to eventId))
            .setInitialDelay(delaySeconds.coerceAtLeast(0), TimeUnit.SECONDS)
            .addTag(AppConstants.WORK_TAG_WEBHOOK)
            .addTag(eventId)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            AppConstants.WORK_NAME_PREFIX + eventId,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun enqueueAllPending(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<WebhookDeliveryWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(WebhookDeliveryWorker.KEY_PROCESS_QUEUE to true))
            .addTag(AppConstants.WORK_TAG_WEBHOOK)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "process_pending_queue",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun ensurePeriodicRetry(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<WebhookDeliveryWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(workDataOf(WebhookDeliveryWorker.KEY_PROCESS_QUEUE to true))
            .addTag(AppConstants.WORK_TAG_WEBHOOK)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_RETRY_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
