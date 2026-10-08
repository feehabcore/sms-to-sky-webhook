package com.payment.smsforwarder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.payment.smsforwarder.data.database.AppDatabase
import com.payment.smsforwarder.data.prefs.SecureSettings
import com.payment.smsforwarder.data.repository.PaymentEventRepository
import com.payment.smsforwarder.network.WebhookClient
import com.payment.smsforwarder.util.AppConstants
import com.payment.smsforwarder.util.DeviceIdProvider
import com.payment.smsforwarder.worker.DeliveryScheduler

class GatewayApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var repository: PaymentEventRepository
        private set
    lateinit var secureSettings: SecureSettings
        private set
    lateinit var webhookClient: WebhookClient
        private set
    lateinit var deviceId: String
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        secureSettings = SecureSettings(this)
        deviceId = DeviceIdProvider.getDeviceId(this)
        database = AppDatabase.getInstance(this)
        repository = PaymentEventRepository(database.paymentEventDao())
        webhookClient = WebhookClient(secureSettings)

        createNotificationChannel()
        DeliveryScheduler.ensurePeriodicRetry(this)

        if (secureSettings.foregroundServiceEnabled && secureSettings.isConfigured()) {
            com.payment.smsforwarder.service.GatewayForegroundService.start(this)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.NOTIFICATION_CHANNEL_ID,
                getString(R.string.foreground_service_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.foreground_service_desc)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        lateinit var instance: GatewayApp
            private set
    }
}
