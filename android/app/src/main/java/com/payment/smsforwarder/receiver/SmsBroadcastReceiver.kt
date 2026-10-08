package com.payment.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.processor.SmsIngress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val app = context.applicationContext as? GatewayApp ?: return
        if (!app.secureSettings.gatewayEnabled) {
            Log.d(TAG, "Gateway disabled — ignoring SMS")
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sender = messages.firstOrNull()?.displayOriginatingAddress.orEmpty()
        val body = messages.joinToString(separator = "") { it.displayMessageBody.orEmpty() }

        val allowed = app.secureSettings.allowedSenders()
        if (allowed.isNotEmpty()) {
            val normalizedSender = sender.trim().uppercase()
            if (allowed.none { normalizedSender.contains(it) }) {
                Log.d(TAG, "Sender filtered out: $sender")
                return
            }
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SmsIngress.process(app, sender, body)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SmsBroadcastReceiver"
    }
}
