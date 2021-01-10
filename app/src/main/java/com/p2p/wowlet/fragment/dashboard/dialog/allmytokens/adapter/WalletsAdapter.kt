package com.p2p.wowlet.fragment.dashboard.dialog.allmytokens.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemAllWalletsBinding
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.wowlet.entities.local.WalletItem

class WalletsAdapter(
    private val viewModel: DashboardViewModel,
    private var list: MutableList<WalletItem>,
    private val itemClickListener: ((WalletItem) -> Unit)
) : RecyclerView.Adapter<WalletsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind: ItemAllWalletsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_all_wallets,
            parent,
            false
        )
        return MyViewHolder(bind)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemWalletsBinding.itemWallet = list[position]
        holder.itemWalletsBinding.viewModel = viewModel
        holder.itemWalletsBinding.itemWalletContainer.setOnClickListener {
            itemClickListener.invoke(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateData(walletItem: List<WalletItem>) {
        list = walletItem.toMutableList()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(
        val itemWalletsBinding: ItemAllWalletsBinding
    ) : RecyclerView.ViewHolder(itemWalletsBinding.root)

}