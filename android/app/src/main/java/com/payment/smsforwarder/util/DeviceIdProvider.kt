package com.payment.smsforwarder.util

import android.content.Context
import android.provider.Settings
import java.util.UUID

object DeviceIdProvider {

    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            "android-$androidId"
        } else {
            "android-${UUID.randomUUID()}"
        }
    }
}
