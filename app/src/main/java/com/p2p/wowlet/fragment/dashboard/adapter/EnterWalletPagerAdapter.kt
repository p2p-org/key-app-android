package com.p2p.wowlet.fragment.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.wowlet.entities.local.EnterWallet
import kotlinx.android.synthetic.main.item_enter_wallet_pager.view.*

class EnterWalletPagerAdapter(private val list: List<EnterWallet>) :
    RecyclerView.Adapter<EnterWalletPagerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_enter_wallet_pager, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindView(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(data: EnterWallet) {
            with(itemView) {
                walletQrCodeIv.setImageBitmap(data.qrCode)
                walletAddressTv.text = data.walletAddress
            }
        }

    }
}