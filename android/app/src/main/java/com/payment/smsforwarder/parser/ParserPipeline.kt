package com.payment.smsforwarder.parser

import com.payment.smsforwarder.data.model.ParserResult

class ParserPipeline(
    private val parsers: List<SmsParser> = listOf(
        BkashParser(),
        NagadParser(),
        RocketParser(),
        UpayParser(),
        GenericParser()
    )
) {
    fun parse(sender: String, body: String, deviceId: String): ParserResult {
        val trimmedBody = body.trim()
        if (trimmedBody.isEmpty()) {
            return ParserResult.Ignored("Empty SMS body")
        }

        val matched = parsers.firstOrNull { it.canHandle(sender, trimmedBody) }
            ?: return ParserResult.Ignored("No payment provider matched")

        return matched.parse(sender, trimmedBody, deviceId)
    }
}
