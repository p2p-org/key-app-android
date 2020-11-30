package com.p2p.wowlet.fragment.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemSecretKeyBinding
import com.p2p.wowlet.databinding.ItemWalletsBinding
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.wowlet.entities.local.WalletItem

class WalletsAdapter(
    private val viewModel: DashboardViewModel,
    private val list: List<WalletItem>
) : RecyclerView.Adapter<WalletsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind: ItemWalletsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_wallets,
            parent,
            false
        )
        return MyViewHolder(bind)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemWalletsBinding.itemWallet = list[position]
        holder.itemWalletsBinding.viewModel = viewModel
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(
        val itemWalletsBinding: ItemWalletsBinding
    ) : RecyclerView.ViewHolder(itemWalletsBinding.root)

}