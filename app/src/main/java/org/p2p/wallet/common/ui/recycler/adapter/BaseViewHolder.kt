package org.p2p.wallet.common.ui.recycler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<in T>(view: View) : RecyclerView.ViewHolder(view) {

    constructor(parent: ViewGroup, @LayoutRes layoutId: Int) :
        this(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

    open fun onBind(item: T) = Unit
    open fun onViewRecycled() = Unit
    open fun onViewAttachedToWindow() = Unit
    open fun onViewDetachedFromWindow() = Unit
}
