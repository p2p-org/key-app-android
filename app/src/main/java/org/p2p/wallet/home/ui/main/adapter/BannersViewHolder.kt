package org.p2p.wallet.home.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemBannersBinding
import org.p2p.wallet.home.model.HomeElementItem

class BannersViewHolder(
    binding: ItemBannersBinding,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        listener: OnHomeItemsClickListener
    ) : this(
        binding = ItemBannersBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    private val bannersAdapter: BannersAdapter by lazy {
        BannersAdapter(listener)
    }

    init {
        with(binding.root) {
            layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
            adapter = bannersAdapter
        }
    }

    fun onBind(item: HomeElementItem.Banners) {
        if (!bannersAdapter.isEmpty()) return

        bannersAdapter.setItems(item.banners)
    }
}
