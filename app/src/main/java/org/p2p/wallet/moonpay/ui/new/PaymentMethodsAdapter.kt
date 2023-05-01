package org.p2p.wallet.moonpay.ui.new

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.moonpay.model.PaymentMethod

class PaymentMethodsAdapter(
    private val onMethodClick: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodViewHolder>() {

    private val items = mutableListOf<PaymentMethod>()
    private var itemWidth: Int = 0

    fun setItems(items: List<PaymentMethod>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        if (itemWidth == 0) {
            // calculate parent width / 2 minus paddings
            itemWidth = (parent.measuredWidth / 2f).toInt() - parent.paddingLeft - parent.paddingRight
        }
        return PaymentMethodViewHolder(parent = parent, onClickListener = onMethodClick).apply {
            itemView.layoutParams = with(itemView.layoutParams as RecyclerView.LayoutParams) {
                // then we get calculated width and add left margin to shift next element to its left margin
                width = (itemWidth + marginStart)
                this
            }
        }
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
