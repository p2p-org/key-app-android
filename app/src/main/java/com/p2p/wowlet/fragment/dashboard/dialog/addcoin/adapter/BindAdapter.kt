package com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.fragment.qrscanner.viewmodel.QrScannerViewModel
import com.wowlet.entities.local.AddCoinItem

@BindingAdapter("adapter_list")
fun RecyclerView.setAdapterList(
    data: List<AddCoinItem>?,
) {
    data?.let {
        adapter = AddCoinAdapter(it)
    }
   this.layoutManager = LinearLayoutManager(this.context)
}