package com.payment.smsforwarder.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.R
import com.payment.smsforwarder.databinding.ActivitySettingsBinding
import com.payment.smsforwarder.service.GatewayForegroundService

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val ok = granted.values.all { it }
        Toast.makeText(
            this,
            if (ok) R.string.toast_permissions_granted else R.string.toast_permissions_denied,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val settings = (application as GatewayApp).secureSettings
        binding.switchGateway.isChecked = settings.gatewayEnabled
        binding.switchForeground.isChecked = settings.foregroundServiceEnabled
        binding.inputDeviceName.setText(settings.deviceName)
        binding.inputWebhookUrl.setText(settings.webhookUrl)
        binding.inputApiToken.setText(settings.apiToken)
        binding.inputHmacSecret.setText(settings.hmacSecret)
        binding.inputFilterSenders.setText(settings.filterSenders)
        binding.deviceIdInfo.text = getString(
            R.string.device_id_label,
            (application as GatewayApp).deviceId
        )

        binding.btnSave.setOnClickListener { saveSettings() }
        binding.btnRequestPermissions.setOnClickListener { requestAllPermissions() }
    }

    private fun saveSettings() {
        val app = application as GatewayApp
        val settings = app.secureSettings
        settings.gatewayEnabled = binding.switchGateway.isChecked
        settings.foregroundServiceEnabled = binding.switchForeground.isChecked
        settings.deviceName = binding.inputDeviceName.text?.toString().orEmpty()
        settings.webhookUrl = binding.inputWebhookUrl.text?.toString().orEmpty()
        settings.apiToken = binding.inputApiToken.text?.toString().orEmpty()
        settings.hmacSecret = binding.inputHmacSecret.text?.toString().orEmpty()
        settings.filterSenders = binding.inputFilterSenders.text?.toString().orEmpty()

        if (settings.foregroundServiceEnabled && settings.isConfigured()) {
            GatewayForegroundService.start(this)
        } else {
            GatewayForegroundService.stop(this)
        }

        Toast.makeText(this, R.string.toast_settings_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun requestAllPermissions() {
        val needed = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            Toast.makeText(this, R.string.toast_permissions_granted, Toast.LENGTH_SHORT).show()
        }
        requestBatteryOptimizationExemption()
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val pm = getSystemService(PowerManager::class.java) ?: return
        if (pm.isIgnoringBatteryOptimizations(packageName)) return
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (_: Exception) {
            // OEM may block this intent
        }
    }
}
