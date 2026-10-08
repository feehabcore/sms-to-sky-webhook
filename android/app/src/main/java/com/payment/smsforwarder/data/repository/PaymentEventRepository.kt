package com.payment.smsforwarder.data.repository

import androidx.lifecycle.LiveData
import com.payment.smsforwarder.data.database.PaymentEventDao
import com.payment.smsforwarder.data.database.PaymentEventEntity
import com.payment.smsforwarder.data.model.DeliveryStatus
import com.payment.smsforwarder.data.model.NormalizedPaymentEvent

class PaymentEventRepository(private val dao: PaymentEventDao) {

    fun observeAll(): LiveData<List<PaymentEventEntity>> = dao.observeAll()

    fun observeRecent(limit: Int = 50): LiveData<List<PaymentEventEntity>> =
        dao.observeRecent(limit)

    fun observeLatest(): LiveData<PaymentEventEntity?> = dao.observeLatest()

    fun observeQueueCount(): LiveData<Int> = dao.observeQueueCount()

    fun observeSentCount(): LiveData<Int> = dao.observeSentCount()

    fun observeFailedCount(): LiveData<Int> = dao.observeFailedCount()

    suspend fun getById(eventId: String): PaymentEventEntity? = dao.getById(eventId)

    suspend fun getPendingOrFailed(): List<PaymentEventEntity> = dao.getPendingOrFailed()

    /**
     * Inserts event if fingerprint is new.
     * @return true if inserted, false if duplicate
     */
    suspend fun insertIfNew(event: NormalizedPaymentEvent, smsSender: String?): InsertResult {
        val existing = dao.getByFingerprint(event.fingerprint)
        if (existing != null) {
            return InsertResult.Duplicate(existing.eventId)
        }
        val entity = PaymentEventEntity.from(event, smsSender)
        val rowId = dao.insert(entity)
        return if (rowId == -1L) {
            InsertResult.Duplicate(event.eventId)
        } else {
            InsertResult.Inserted(entity)
        }
    }

    suspend fun markSending(eventId: String, attemptCount: Int) {
        dao.updateDelivery(
            eventId = eventId,
            status = DeliveryStatus.SENDING.name,
            attemptCount = attemptCount,
            lastError = null
        )
    }

    suspend fun markSent(eventId: String, attemptCount: Int) {
        dao.updateDelivery(
            eventId = eventId,
            status = DeliveryStatus.SENT.name,
            attemptCount = attemptCount,
            lastError = null,
            deliveredAt = System.currentTimeMillis()
        )
    }

    suspend fun markFailed(eventId: String, attemptCount: Int, error: String) {
        dao.updateDelivery(
            eventId = eventId,
            status = DeliveryStatus.FAILED.name,
            attemptCount = attemptCount,
            lastError = error.take(500)
        )
    }

    sealed class InsertResult {
        data class Inserted(val entity: PaymentEventEntity) : InsertResult()
        data class Duplicate(val existingEventId: String) : InsertResult()
    }
}
