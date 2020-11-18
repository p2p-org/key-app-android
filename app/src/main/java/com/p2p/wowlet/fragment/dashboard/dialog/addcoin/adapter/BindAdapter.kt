package com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wowlet.entities.local.ConstWalletItem

@BindingAdapter("adapter_list")
fun RecyclerView.setAdapterList(
    data: List<ConstWalletItem>?,
) {
    data?.let {
        adapter = AddCoinAdapter(it)
    }
   this.layoutManager = LinearLayoutManager(this.context)
}