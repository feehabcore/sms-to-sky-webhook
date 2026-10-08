package com.payment.smsforwarder.ui.simulator

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.R
import com.payment.smsforwarder.databinding.ActivitySimulatorBinding
import com.payment.smsforwarder.processor.SmsIngress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimulatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimulatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimulatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnLoadBkash.setOnClickListener {
            binding.inputSender.setText("bKash")
            binding.inputBody.setText(getString(R.string.sample_bkash_sms))
        }
        binding.btnLoadNagad.setOnClickListener {
            binding.inputSender.setText("NAGAD")
            binding.inputBody.setText(getString(R.string.sample_nagad_sms))
        }
        binding.btnParse.setOnClickListener { parseAndQueue() }
    }

    private fun parseAndQueue() {
        val sender = binding.inputSender.text?.toString().orEmpty()
        val body = binding.inputBody.text?.toString().orEmpty()
        val app = application as GatewayApp

        lifecycleScope.launch {
            val message = withContext(Dispatchers.IO) {
                when (val outcome = SmsIngress.process(app, sender, body)) {
                    is SmsIngress.Outcome.Queued ->
                        "QUEUED\nProvider: ${outcome.provider}\nTrx: ${outcome.transactionId}\nEvent: ${outcome.eventId}"
                    is SmsIngress.Outcome.Duplicate ->
                        "DUPLICATE — already processed (${outcome.existingEventId})"
                    is SmsIngress.Outcome.Ignored ->
                        "IGNORED: ${outcome.reason}"
                    is SmsIngress.Outcome.Unknown ->
                        "UNKNOWN: ${outcome.reason}"
                }
            }
            binding.resultText.text = message
            Toast.makeText(
                this@SimulatorActivity,
                message.lineSequence().first(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
