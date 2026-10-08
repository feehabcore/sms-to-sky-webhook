package com.payment.smsforwarder.ui.events

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.payment.smsforwarder.R
import com.payment.smsforwarder.data.database.PaymentEventEntity
import com.payment.smsforwarder.data.model.DeliveryStatus
import com.payment.smsforwarder.databinding.ItemEventBinding

class EventsAdapter : ListAdapter<PaymentEventEntity, EventsAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PaymentEventEntity) {
            binding.providerText.text = item.provider.uppercase()
            binding.amountText.text = "৳ ${"%.2f".format(item.amount)} ${item.currency}"
            binding.statusText.text = item.deliveryStatus
            binding.detailsText.text = buildString {
                appendLine("Trx: ${item.transactionId}")
                appendLine("From: ${item.senderIdentifier}")
                appendLine("Attempts: ${item.attemptCount}")
                if (!item.lastError.isNullOrBlank()) appendLine("Error: ${item.lastError}")
                append(item.timestamp)
            }
            val color = when (item.deliveryStatus) {
                DeliveryStatus.SENT.name -> Color.parseColor("#10B981")
                DeliveryStatus.FAILED.name -> Color.parseColor("#EF4444")
                DeliveryStatus.PENDING.name, DeliveryStatus.SENDING.name -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#64748B")
            }
            binding.statusText.setTextColor(color)
            binding.statusText.setBackgroundResource(R.drawable.bg_status_chip)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PaymentEventEntity>() {
            override fun areItemsTheSame(a: PaymentEventEntity, b: PaymentEventEntity) =
                a.eventId == b.eventId

            override fun areContentsTheSame(a: PaymentEventEntity, b: PaymentEventEntity) =
                a == b
        }
    }
}
