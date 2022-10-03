package org.p2p.wallet.solend.ui.earn.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.solend.model.SolendDepositToken

class SolendEarnAdapter : RecyclerView.Adapter<SolendEarnViewHolder>() {

    private val data = mutableListOf<SolendDepositToken>()

    fun setItems(new: List<SolendDepositToken>) {
        data.clear()
        data += new
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolendEarnViewHolder =
        SolendEarnViewHolder(parent)

    override fun onBindViewHolder(holder: SolendEarnViewHolder, position: Int) {
        holder.onBind(data[position])
    }
}
