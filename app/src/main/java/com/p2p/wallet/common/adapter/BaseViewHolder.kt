package com.p2p.wallet.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>(
    @LayoutRes layoutId: Int,
    parent: ViewGroup,
    private val onItemClickListener: ((T) -> Unit)? = null
) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false)) {

    fun performBind(item: T) {
        onItemClickListener?.let { itemView.setOnClickListener { it(item) } }
        onBind(item)
    }

    protected abstract fun onBind(item: T)

    open fun onViewRecycled() = Unit
}