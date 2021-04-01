package com.p2p.wowlet.fragment.dashboard.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.wowlet.entities.local.WalletItem

@BindingAdapter("adapter_list", "view_model")
fun RecyclerView.setAdapterList(
    data: List<WalletItem>?,
    viewModel: DashboardViewModel
) {
    data?.let {
        adapter = WalletsAdapter(viewModel, it)
    }
   this.layoutManager = LinearLayoutManager(this.context)
}


