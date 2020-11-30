package com.p2p.wowlet.fragment.swap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemMyWalletsBinding
import com.p2p.wowlet.databinding.MyWalletHeaderBinding
import com.p2p.wowlet.fragment.swap.viewmodel.SwapViewModel
import com.wowlet.entities.CoinType
import com.wowlet.entities.local.CoinItem
import com.wowlet.entities.local.WalletItem

class YourWalletsAdapter(
    private val viewModel: SwapViewModel,
    private val list: List<WalletItem>
) : RecyclerView.Adapter<YourWalletsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind: ItemMyWalletsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_my_wallets,
            parent,
            false
        )
        return MyViewHolder(bind)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemWalletsBinding.walletItem = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(
        val itemWalletsBinding: ItemMyWalletsBinding
    ) : RecyclerView.ViewHolder(itemWalletsBinding.root)

}