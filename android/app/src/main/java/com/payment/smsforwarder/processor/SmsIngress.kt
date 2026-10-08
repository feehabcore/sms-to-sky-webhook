package com.payment.smsforwarder.processor

import android.util.Log
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.data.model.ParserResult
import com.payment.smsforwarder.data.repository.PaymentEventRepository
import com.payment.smsforwarder.parser.ParserPipeline
import com.payment.smsforwarder.worker.DeliveryScheduler

/**
 * Shared SMS → parse → queue → schedule delivery pipeline.
 */
object SmsIngress {

    private const val TAG = "SmsIngress"
    private val parser = ParserPipeline()

    sealed class Outcome {
        data class Queued(val eventId: String, val transactionId: String, val provider: String) : Outcome()
        data class Duplicate(val existingEventId: String) : Outcome()
        data class Ignored(val reason: String) : Outcome()
        data class Unknown(val reason: String) : Outcome()
    }

    suspend fun process(app: GatewayApp, sender: String, body: String): Outcome {
        return when (val result = parser.parse(sender, body, app.deviceId)) {
            is ParserResult.Success -> {
                when (val insert = app.repository.insertIfNew(result.event, sender)) {
                    is PaymentEventRepository.InsertResult.Inserted -> {
                        DeliveryScheduler.enqueueEvent(app, result.event.eventId)
                        Log.i(TAG, "Queued ${result.event.provider} ${result.event.transactionId}")
                        Outcome.Queued(
                            eventId = result.event.eventId,
                            transactionId = result.event.transactionId,
                            provider = result.event.provider
                        )
                    }
                    is PaymentEventRepository.InsertResult.Duplicate -> {
                        Log.i(TAG, "Duplicate ${result.event.transactionId}")
                        Outcome.Duplicate(insert.existingEventId)
                    }
                }
            }
            is ParserResult.Ignored -> Outcome.Ignored(result.reason)
            is ParserResult.Unknown -> Outcome.Unknown(result.reason)
        }
    }
}
