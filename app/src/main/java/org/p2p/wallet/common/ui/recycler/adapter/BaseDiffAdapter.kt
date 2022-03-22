package org.p2p.wallet.common.ui.recycler.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView

abstract class BaseDiffAdapter<T> : RecyclerView.Adapter<BaseViewHolder<T>>() {
    abstract val mDiffer: AsyncListDiffer<T>

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) =
        holder.onBind(mDiffer.currentList[position])

    override fun getItemCount() = mDiffer.currentList.size

    override fun onViewRecycled(holder: BaseViewHolder<T>) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    fun isEmpty(): Boolean = mDiffer.currentList.isEmpty()

    fun isNotEmpty(): Boolean = mDiffer.currentList.isNotEmpty()

    fun clear() {
        mDiffer.submitList(emptyList())
    }

    fun setItems(items: List<T>) {
        mDiffer.submitList(items)
    }

    fun setItems(items: List<T>, commitCallback: () -> Unit) {
        mDiffer.submitList(items, commitCallback)
    }
}
