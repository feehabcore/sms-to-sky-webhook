package com.payment.smsforwarder.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureSettings(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        // Fallback for emulators / keystore issues during development
        context.getSharedPreferences(PREFS_NAME_FALLBACK, Context.MODE_PRIVATE)
    }

    var webhookUrl: String
        get() = prefs.getString(KEY_WEBHOOK_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBHOOK_URL, value.trim()).apply()

    var apiToken: String
        get() = prefs.getString(KEY_API_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_TOKEN, value.trim()).apply()

    var hmacSecret: String
        get() = prefs.getString(KEY_HMAC_SECRET, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HMAC_SECRET, value.trim()).apply()

    var deviceName: String
        get() = prefs.getString(KEY_DEVICE_NAME, "SMS Gateway") ?: "SMS Gateway"
        set(value) = prefs.edit().putString(KEY_DEVICE_NAME, value.trim()).apply()

    var gatewayEnabled: Boolean
        get() = prefs.getBoolean(KEY_GATEWAY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_GATEWAY_ENABLED, value).apply()

    var foregroundServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_FOREGROUND_SERVICE, true)
        set(value) = prefs.edit().putBoolean(KEY_FOREGROUND_SERVICE, value).apply()

    var filterSenders: String
        get() = prefs.getString(KEY_FILTER_SENDERS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_FILTER_SENDERS, value.trim()).apply()

    fun isConfigured(): Boolean =
        webhookUrl.isNotBlank() && apiToken.isNotBlank() && hmacSecret.isNotBlank()

    fun allowedSenders(): List<String> =
        filterSenders
            .split(',', ';', '\n')
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }

    companion object {
        private const val PREFS_NAME = "gateway_secure_prefs"
        private const val PREFS_NAME_FALLBACK = "gateway_prefs_fallback"
        private const val KEY_WEBHOOK_URL = "webhook_url"
        private const val KEY_API_TOKEN = "api_token"
        private const val KEY_HMAC_SECRET = "hmac_secret"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_GATEWAY_ENABLED = "gateway_enabled"
        private const val KEY_FOREGROUND_SERVICE = "foreground_service"
        private const val KEY_FILTER_SENDERS = "filter_senders"
    }
}
