package org.p2p.wallet.moonpay.ui.new

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.moonpay.model.PaymentMethod

class PaymentMethodsAdapter(
    private val onMethodClick: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodViewHolder>() {

    private val items = mutableListOf<PaymentMethod>()

    fun setItems(items: List<PaymentMethod>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        return PaymentMethodViewHolder(parent = parent, onClickListener = onMethodClick)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
