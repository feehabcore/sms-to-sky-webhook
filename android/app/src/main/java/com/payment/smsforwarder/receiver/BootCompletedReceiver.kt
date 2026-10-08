package com.payment.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.service.GatewayForegroundService
import com.payment.smsforwarder.worker.DeliveryScheduler

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        Log.i(TAG, "Boot/replace received — restoring gateway workers")
        DeliveryScheduler.ensurePeriodicRetry(context)
        DeliveryScheduler.enqueueAllPending(context)

        val app = context.applicationContext as? GatewayApp
        if (app != null &&
            app.secureSettings.foregroundServiceEnabled &&
            app.secureSettings.isConfigured()
        ) {
            GatewayForegroundService.start(context)
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
