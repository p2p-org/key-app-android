package com.p2p.wallet.dashboard.ui.dialog.allmytokens.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.p2p.wallet.dashboard.ui.viewmodel.DashboardViewModel
import com.p2p.wallet.databinding.ItemAllWalletsBinding
import com.p2p.wallet.dashboard.model.local.WalletItem

class WalletsAdapter(
    private val viewModel: DashboardViewModel,
    private var list: MutableList<WalletItem>,
    private val itemClickListener: ((WalletItem) -> Unit)
) : RecyclerView.Adapter<WalletsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind = ItemAllWalletsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(bind)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateData(walletItem: List<WalletItem>) {
        list = walletItem.toMutableList()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(
        private val itemWalletsBinding: ItemAllWalletsBinding
    ) : RecyclerView.ViewHolder(itemWalletsBinding.root) {

        private val vItem = itemWalletsBinding.vItem
        private val vWalletAddress = itemWalletsBinding.vWalletAddress
        private val vCoin = itemWalletsBinding.vCoin
        private val itemWalletContainer = itemWalletsBinding.itemWalletContainer

        fun onBind(item: WalletItem) {
            vItem.text = item.tokenName
            vWalletAddress.text = item.depositAddress
            Glide.with(vCoin).load(item.icon).into(vCoin)
            itemWalletContainer.setOnClickListener {
                itemClickListener.invoke(list[position])
            }
        }
    }
}