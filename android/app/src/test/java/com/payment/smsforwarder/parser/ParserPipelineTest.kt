package com.payment.smsforwarder.parser

import com.payment.smsforwarder.data.model.ParserResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserPipelineTest {

    private val pipeline = ParserPipeline()
    private val deviceId = "android-test"

    @Test
    fun parsesBkashIncomingPayment() {
        val body =
            "You have received Tk 500.00 from 01712345678. Fee Tk 0.00. Balance Tk 1200.00. TrxID 8A1B2C3D4E at 08/10/2026 10:15"
        val result = pipeline.parse("bKash", body, deviceId)
        assertTrue(result is ParserResult.Success)
        val event = (result as ParserResult.Success).event
        assertEquals("bkash", event.provider)
        assertEquals(500.0, event.amount, 0.001)
        assertEquals("8A1B2C3D4E", event.transactionId)
        assertEquals("01712345678", event.senderIdentifier)
    }

    @Test
    fun parsesNagadIncomingPayment() {
        val body =
            "You have received money Tk 250.00 from 01876543210. Your current Nagad account balance is Tk 900.00. Transaction ID: NGD99887766."
        val result = pipeline.parse("NAGAD", body, deviceId)
        assertTrue(result is ParserResult.Success)
        val event = (result as ParserResult.Success).event
        assertEquals("nagad", event.provider)
        assertEquals(250.0, event.amount, 0.001)
        assertEquals("NGD99887766", event.transactionId)
    }

    @Test
    fun ignoresNonPaymentSms() {
        val result = pipeline.parse("bKash", "Your OTP is 123456", deviceId)
        assertTrue(result is ParserResult.Ignored)
    }
}
