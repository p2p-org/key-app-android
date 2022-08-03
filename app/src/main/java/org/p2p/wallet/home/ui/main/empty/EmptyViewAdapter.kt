package org.p2p.wallet.home.ui.main.empty

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.ui.main.adapter.OnHomeItemsClickListener

class EmptyViewAdapter(
    private val listener: OnHomeItemsClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Any>()

    private var isZerosHidden: Boolean = true

    fun setItems(items: List<Any>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is HomeBannerItem -> R.layout.item_big_banner
        else -> throw IllegalStateException("Unknown viewType: ${data[position]}")
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_big_banner -> BigBannerViewHolder(parent) { bannerId: Int -> listener.onBannerClicked(bannerId) }
        R.layout.item_get_token -> GetTokenViewHolder(parent, listener)
        else -> error("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BigBannerViewHolder -> holder.onBind(data[position] as HomeBannerItem)
            is GetTokenViewHolder -> holder.onBind(data[position] as HomeElementItem.Shown, isZerosHidden)
        }
    }
}
