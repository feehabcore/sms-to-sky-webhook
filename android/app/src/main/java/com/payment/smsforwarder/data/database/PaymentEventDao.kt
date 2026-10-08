package com.payment.smsforwarder.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PaymentEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: PaymentEventEntity): Long

    @Query("SELECT * FROM payment_events ORDER BY created_at DESC")
    fun observeAll(): LiveData<List<PaymentEventEntity>>

    @Query("SELECT * FROM payment_events ORDER BY created_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): LiveData<List<PaymentEventEntity>>

    @Query("SELECT * FROM payment_events WHERE event_id = :eventId LIMIT 1")
    suspend fun getById(eventId: String): PaymentEventEntity?

    @Query("SELECT * FROM payment_events WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): PaymentEventEntity?

    @Query(
        """
        SELECT * FROM payment_events
        WHERE delivery_status IN ('PENDING', 'FAILED')
        ORDER BY created_at ASC
        """
    )
    suspend fun getPendingOrFailed(): List<PaymentEventEntity>

    @Query("SELECT COUNT(*) FROM payment_events WHERE delivery_status = :status")
    fun observeCountByStatus(status: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM payment_events WHERE delivery_status IN ('PENDING', 'SENDING', 'FAILED')")
    fun observeQueueCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM payment_events WHERE delivery_status = 'SENT'")
    fun observeSentCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM payment_events WHERE delivery_status = 'FAILED'")
    fun observeFailedCount(): LiveData<Int>

    @Query("SELECT * FROM payment_events ORDER BY created_at DESC LIMIT 1")
    fun observeLatest(): LiveData<PaymentEventEntity?>

    @Query(
        """
        UPDATE payment_events SET
            delivery_status = :status,
            attempt_count = :attemptCount,
            last_error = :lastError,
            updated_at = :updatedAt,
            delivered_at = :deliveredAt
        WHERE event_id = :eventId
        """
    )
    suspend fun updateDelivery(
        eventId: String,
        status: String,
        attemptCount: Int,
        lastError: String?,
        updatedAt: Long = System.currentTimeMillis(),
        deliveredAt: Long? = null
    )

    @Query("DELETE FROM payment_events WHERE created_at < :beforeEpochMs")
    suspend fun deleteOlderThan(beforeEpochMs: Long): Int
}
