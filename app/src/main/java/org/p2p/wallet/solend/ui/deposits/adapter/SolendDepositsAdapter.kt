package org.p2p.wallet.solend.ui.deposits.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.solend.model.SolendDepositToken

class SolendDepositsAdapter(
    private val depositClickListener: TokenDepositItemClickListener
) : RecyclerView.Adapter<SolendDepositViewHolder>() {

    private val data = mutableListOf<SolendDepositToken>()

    fun setItems(new: List<SolendDepositToken>) {
        data.clear()
        data += new
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SolendDepositViewHolder = SolendDepositViewHolder(
        parent,
        onAddMoreClicked = depositClickListener::onAddMoreClicked,
        onWithdrawClicked = depositClickListener::onWithdrawClicked
    )

    override fun onBindViewHolder(holder: SolendDepositViewHolder, position: Int) {
        holder.onBind(data[position])
    }
}

interface TokenDepositItemClickListener {
    fun onAddMoreClicked(token: SolendDepositToken)
    fun onWithdrawClicked(token: SolendDepositToken)
}
