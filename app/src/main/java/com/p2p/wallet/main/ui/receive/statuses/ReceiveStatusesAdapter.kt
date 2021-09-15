package com.p2p.wallet.main.ui.receive.statuses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.databinding.ItemReceiveStatusBinding
import com.p2p.wallet.main.model.ReceiveStatus

class ReceiveStatusesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<ReceiveStatus>()

    fun setItems(new: List<ReceiveStatus>) {
        val before = data.size
        data.addAll(new)
        notifyItemRangeInserted(before, data.size - 1)
    }

    fun addItem(status: ReceiveStatus) {
        data.add(status)
        notifyItemInserted(data.size - 1)
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).onBind(data[position])
    }

    private inner class ViewHolder(
        private val binding: ItemReceiveStatusBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            ItemReceiveStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        fun onBind(item: ReceiveStatus) {
            with(binding) {
                titleTextView.text = item.name
                subTitleTextView.text = item.date
                amountTextView.text = item.amount
            }
        }
    }
}