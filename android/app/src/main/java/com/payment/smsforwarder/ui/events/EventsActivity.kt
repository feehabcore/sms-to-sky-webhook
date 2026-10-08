package com.payment.smsforwarder.ui.events

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.payment.smsforwarder.GatewayApp
import com.payment.smsforwarder.databinding.ActivityEventsBinding

class EventsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventsBinding
    private val adapter = EventsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.eventsRecycler.layoutManager = LinearLayoutManager(this)
        binding.eventsRecycler.adapter = adapter

        val app = application as GatewayApp
        app.repository.observeAll().observe(this) { list ->
            adapter.submitList(list)
            binding.emptyText.visibility = if (list.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }
}
