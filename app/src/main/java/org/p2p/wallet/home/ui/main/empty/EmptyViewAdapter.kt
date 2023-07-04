package org.p2p.wallet.home.ui.main.empty

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.ui.main.adapter.HeaderViewHolder
import org.p2p.wallet.home.ui.main.adapter.HomeItemsClickListeners

class EmptyViewAdapter(
    private val listener: HomeItemsClickListeners
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Any>()

    fun setItems(items: List<Any>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is HomeBannerItem -> R.layout.item_big_banner
        is Token -> R.layout.item_popular_token
        is String -> R.layout.item_main_header
        else -> throw IllegalStateException("Unknown viewType: ${data[position]}")
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_big_banner -> BigBannerViewHolder(parent) { bannerId: Int ->
            listener.onBannerClicked(bannerId)
        }
        R.layout.item_popular_token -> PopularTokenViewHolder(parent) { popularToken ->
            listener.onPopularTokenClicked(popularToken)
        }
        R.layout.item_main_header -> HeaderViewHolder(parent)

        else -> error("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BigBannerViewHolder -> holder.onBind(data[position] as HomeBannerItem)
            is PopularTokenViewHolder -> holder.onBind(data[position] as Token)
            is HeaderViewHolder -> holder.onBind(data[position] as String)
        }
    }
}
