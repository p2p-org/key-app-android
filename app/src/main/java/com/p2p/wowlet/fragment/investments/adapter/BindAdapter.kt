package com.p2p.wowlet.fragment.investments.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.wowlet.entities.local.SecretKeyItem

@BindingAdapter("adapter_list", "view_model")
fun RecyclerView.setAdapterList(
    data: List<SecretKeyItem>?,
    viewModel: SecretKeyViewModel
) {
    data?.let {
       // adapter = SecretKeyAdapter(viewModel, it)
    }
   this.layoutManager = GridLayoutManager(this.context,3)
}