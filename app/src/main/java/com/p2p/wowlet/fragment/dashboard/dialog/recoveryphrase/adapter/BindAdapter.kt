package com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.wowlet.entities.local.RecoveryPhraseItem

@BindingAdapter("adapter_list", "view_model")
fun RecyclerView.setAdapterList(
    data: List<String>?,
    viewModel: RecoveryPhraseViewModel
) {
    data?.let {
        adapter = RecoveryPhraseAdapter(viewModel, it)
    }
    this.layoutManager = GridLayoutManager(this.context, 2)
}
