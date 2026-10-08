package com.payment.smsforwarder.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.R
import com.payment.smsforwarder.databinding.ActivityDashboardBinding
import com.payment.smsforwarder.ui.events.EventsActivity
import com.payment.smsforwarder.ui.settings.SettingsActivity
import com.payment.smsforwarder.ui.simulator.SimulatorActivity
import com.payment.smsforwarder.worker.DeliveryScheduler
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(application as GatewayApp)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* results observed via status refresh */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.deviceIdText.text = getString(R.string.device_id_label, viewModel.deviceId())

        viewModel.queueCount.observe(this) {
            binding.queueCount.text = (it ?: 0).toString()
        }
        viewModel.sentCount.observe(this) {
            binding.sentCount.text = (it ?: 0).toString()
        }
        viewModel.failedCount.observe(this) {
            binding.failedCount.text = (it ?: 0).toString()
        }
        viewModel.latestEvent.observe(this) { event ->
            binding.lastEventText.text = if (event == null) {
                getString(R.string.no_events_yet)
            } else {
                buildString {
                    appendLine("${event.provider.uppercase()} · ${event.deliveryStatus}")
                    appendLine("৳ ${"%.2f".format(event.amount)} · Trx ${event.transactionId}")
                    appendLine("From ${event.senderIdentifier}")
                    append(event.timestamp)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toast.collect { msg ->
                    Toast.makeText(this@DashboardActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnTestWebhook.setOnClickListener { viewModel.testWebhook() }
        binding.btnRetryPending.setOnClickListener {
            DeliveryScheduler.enqueueAllPending(this)
            Toast.makeText(this, R.string.toast_retry_queued, Toast.LENGTH_SHORT).show()
        }
        binding.btnViewEvents.setOnClickListener {
            startActivity(Intent(this, EventsActivity::class.java))
        }
        binding.btnSimulateSms.setOnClickListener {
            startActivity(Intent(this, SimulatorActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        requestNeededPermissions()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val online = viewModel.isOnlineConfigured()
        binding.statusBadge.text = getString(
            if (online) R.string.status_connected else R.string.status_offline
        )
        binding.statusBadge.setBackgroundResource(
            if (online) R.drawable.bg_status_online else R.drawable.bg_status_offline
        )
    }

    private fun requestNeededPermissions() {
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
        }
    }
}
