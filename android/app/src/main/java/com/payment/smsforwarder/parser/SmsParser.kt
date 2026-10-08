package com.payment.smsforwarder.parser

import com.payment.smsforwarder.data.model.ParserResult

interface SmsParser {
    val provider: String
    fun canHandle(sender: String, body: String): Boolean
    fun parse(sender: String, body: String, deviceId: String): ParserResult
}
