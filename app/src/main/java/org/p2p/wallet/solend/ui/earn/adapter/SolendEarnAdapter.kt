package org.p2p.wallet.solend.ui.earn.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.solend.model.SolendDepositToken

class SolendEarnAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<SolendDepositToken>()

    fun setItems(new: List<SolendDepositToken>) {
        data.clear()
        data += new
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        SolanaEarnViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SolanaEarnViewHolder).onBind(data[position])
    }
}
