package org.p2p.wallet.history.ui.token.adapter

import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.wallet.history.ui.token.adapter.holders.MoonpayTransactionViewHolder

class HistoryMoonpayAdapter(
    private val onTransactionClicked: (MoonpayTransactionItem) -> Unit,
) : RecyclerView.Adapter<MoonpayTransactionViewHolder>() {

    private val currentItems = mutableListOf<MoonpayTransactionItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun setTransactions(newTransactions: List<MoonpayTransactionItem>) {
        currentItems.clear()
        currentItems.addAll(newTransactions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoonpayTransactionViewHolder {
        return MoonpayTransactionViewHolder(parent)
    }

    override fun onBindViewHolder(holder: MoonpayTransactionViewHolder, position: Int) {
        holder.onBind(currentItems[position])
    }

    override fun getItemCount(): Int = currentItems.size
}
