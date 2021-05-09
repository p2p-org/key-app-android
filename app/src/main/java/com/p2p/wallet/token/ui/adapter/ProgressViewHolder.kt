package com.p2p.wallet.token.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.databinding.ItemProgressBinding

class ProgressViewHolder(
    binding: ItemProgressBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        ItemProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
}