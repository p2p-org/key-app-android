package com.p2p.wallet.token.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.databinding.ItemProgressbarBinding

class ProgressViewHolder(
    binding: ItemProgressbarBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        ItemProgressbarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
}