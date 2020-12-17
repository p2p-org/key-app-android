package com.p2p.wowlet.fragment.dashboard.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemEnterWalletPagerBinding
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.inflate
import com.p2p.wowlet.utils.shareText
import com.wowlet.entities.local.EnterWallet

class EnterWalletPagerAdapter(private val list: List<EnterWallet>) :
    RecyclerView.Adapter<EnterWalletPagerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(parent.inflate(R.layout.item_enter_wallet_pager))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemEnterWalletPagerBinding.apply {
            itemModel = list[position]
            copyWallet.setOnClickListener {
                it.context.copyClipboard(list[position].walletAddress)
            }
            shareWallet.setOnClickListener {
                it.context.shareText(list[position].walletAddress)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(
        val itemEnterWalletPagerBinding: ItemEnterWalletPagerBinding
    ) : RecyclerView.ViewHolder(itemEnterWalletPagerBinding.root)

}