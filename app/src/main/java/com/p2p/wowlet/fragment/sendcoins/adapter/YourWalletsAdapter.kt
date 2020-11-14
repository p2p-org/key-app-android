package com.example.wowlet.fragment.sendcoins.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemMyWalletsBinding
import com.p2p.wowlet.databinding.MyWalletHeaderBinding
import com.p2p.wowlet.fragment.sendcoins.viewmodel.SendCoinsViewModel
import com.wowlet.entities.CoinType
import com.wowlet.entities.local.CoinItem

class YourWalletsAdapter(
    private val viewModel: SendCoinsViewModel,
    private val list: List<CoinItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            CoinType.FOOTER -> {
                val bind: ItemMyWalletsBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_my_wallets,
                    parent,
                    false
                )
                return MyViewHolder(bind)
            }
            CoinType.HEADER -> {
                val bind: MyWalletHeaderBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.my_wallet_header,
                    parent,
                    false
                )
                return MyHeaderViewHolder(bind)
            }
            else -> {
                val bind: ItemMyWalletsBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_my_wallets,
                    parent,
                    false
                )
                return MyViewHolder(bind)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyViewHolder ->
                holder.itemWalletsBinding.coinItem = list[position]
            is MyHeaderViewHolder ->
                holder.itemWalletsBinding.coinItem = list[position]
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list.isNotEmpty()) {
            val item = list[position]
            if (item.type == "")
                CoinType.HEADER
            else
                CoinType.FOOTER
        } else
            super.getItemViewType(position)
    }

    inner class MyViewHolder(
        val itemWalletsBinding: ItemMyWalletsBinding
    ) : RecyclerView.ViewHolder(itemWalletsBinding.root)

    inner class MyHeaderViewHolder(
        val itemWalletsBinding: MyWalletHeaderBinding
    ) : RecyclerView.ViewHolder(itemWalletsBinding.root)

}