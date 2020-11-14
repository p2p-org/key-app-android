package com.p2p.wowlet.fragment.qrscanner.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.fragment.qrscanner.viewmodel.QrScannerViewModel
import com.wowlet.entities.local.AddCoinItem

@BindingAdapter("adapter_list", "view_model")
fun RecyclerView.setAdapterList(
    data: List<AddCoinItem>?,
    viewModel: QrScannerViewModel
) {
    data?.let {
        adapter = AddCoinAdapter(viewModel, it)
    }
   this.layoutManager = LinearLayoutManager(this.context)
}