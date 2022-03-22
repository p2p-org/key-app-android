package org.p2p.wallet.home.ui.main.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.home.model.Banner

class BannersAdapter(
    private val listener: OnHomeItemsClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Banner>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(banners: List<Banner>) {
        data.clear()
        data.addAll(banners)
        notifyDataSetChanged()
    }

    fun isEmpty(): Boolean = data.isEmpty()

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        SingleBannerViewHolder(parent, listener)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SingleBannerViewHolder).onBind(data[position])
    }
}
