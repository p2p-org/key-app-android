package com.p2p.wowlet.fragment.dashboard.dialog.swap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemSwapTokenBinding
import com.wowlet.entities.local.WalletItem
import kotlin.collections.ArrayList

class SelectTokenToSwapAdapter(
    private val selectedWalletItem: WalletItem
) : RecyclerView.Adapter<SelectTokenToSwapAdapter.ViewHolder>() {

    private val swapToTokenItems: MutableList<WalletItem> = mutableListOf()
    private val swapToTokenItemsInitial: MutableList<WalletItem> = mutableListOf()
    private var onItemClick: ((selectedWalletItem: WalletItem) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemSwapTokenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                walletItem = swapToTokenItems[position]
                root.setOnClickListener { onItemClick?.invoke(swapToTokenItems[position]) }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_swap_token,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return swapToTokenItems.size
    }


    private fun filterItemsByName(name: String) {
        if (name.isNotEmpty()) {
            val swapToTokenItemsFiltered: MutableList<WalletItem> = ArrayList()
            swapToTokenItemsInitial.forEach {
                if (it.tokenName.contains(name, true)) {
                    swapToTokenItemsFiltered.add(it)
                }
            }
            updateItems(swapToTokenItemsFiltered)
        }else {
            updateItems(swapToTokenItemsInitial)
        }
    }

    private fun updateItems(walletItems: Collection<WalletItem>) {
        val walletItemsWithoutFrom = walletItems.toMutableList()
        walletItemsWithoutFrom.removeAll { it.mintAddress == selectedWalletItem.mintAddress }
        swapToTokenItems.clear()
        swapToTokenItems.addAll(walletItemsWithoutFrom)
        notifyDataSetChanged()
    }

    fun setSearchBarEditText(editText: AppCompatEditText) {
        editText.doAfterTextChanged {
            filterItemsByName(it.toString())
        }
    }


    fun initList(walletItems: Collection<WalletItem>) {
        swapToTokenItemsInitial.apply {
            clear()
            addAll(walletItems)
        }
        updateItems(swapToTokenItemsInitial)
    }

    fun setOnItemClickListener(clickEvent: (selectedWalletItem: WalletItem) -> Unit) {
        onItemClick = clickEvent
    }



}