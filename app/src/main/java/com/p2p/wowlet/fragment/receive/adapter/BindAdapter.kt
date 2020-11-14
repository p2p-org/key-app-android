package com.p2p.wowlet.fragment.receive.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.fragment.receive.viewmodel.ReceiveViewModel
import com.wowlet.entities.local.ActivityItem

@BindingAdapter("adapter_list", "view_model")
fun RecyclerView.setAdapterList(
    data: List<ActivityItem>?,
    viewModel: ReceiveViewModel
) {
    data?.let {
        adapter = ActivityAdapter(viewModel, it)
    }
   this.layoutManager = LinearLayoutManager(this.context)
}