package com.payment.smsforwarder.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.data.database.PaymentEventEntity
import com.payment.smsforwarder.network.WebhookClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val app: GatewayApp) : ViewModel() {

    val queueCount: LiveData<Int> = app.repository.observeQueueCount()
    val sentCount: LiveData<Int> = app.repository.observeSentCount()
    val failedCount: LiveData<Int> = app.repository.observeFailedCount()
    val latestEvent: LiveData<PaymentEventEntity?> = app.repository.observeLatest()

    private val _toast = MutableSharedFlow<String>()
    val toast: SharedFlow<String> = _toast

    fun isOnlineConfigured(): Boolean =
        app.secureSettings.gatewayEnabled && app.secureSettings.isConfigured()

    fun deviceId(): String = app.deviceId

    fun testWebhook() {
        viewModelScope.launch {
            when (val result = app.webhookClient.testConnection()) {
                is WebhookClient.Result.Success ->
                    _toast.emit("Webhook OK (HTTP ${result.httpCode})")
                is WebhookClient.Result.Failure ->
                    _toast.emit("Webhook failed: ${result.message}")
            }
        }
    }

    class Factory(private val app: GatewayApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(app) as T
        }
    }
}
