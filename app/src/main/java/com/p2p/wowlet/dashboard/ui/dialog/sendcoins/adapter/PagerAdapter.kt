package com.p2p.wowlet.dashboard.ui.dialog.sendcoins.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.dashboard.model.local.UserWalletType
import com.p2p.wowlet.dashboard.ui.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.databinding.ItemSendCoinTypeBinding

class PagerAdapter(
    private val list: List<UserWalletType>,
    private val viewModel: SendCoinsViewModel
) : RecyclerView.Adapter<PagerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemSendCoinTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindView(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(
        private val binding: ItemSendCoinTypeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(data: UserWalletType) {
            with(binding) {
                if (data.isContact)
                    walletUserIconIv.visibility = View.VISIBLE
                else
                    walletUserIconIv.visibility = View.GONE
                paymentTypeTv.text = data.walletType
                walletUserContactTv.text = data.userContact
                imgScanQrCode.setImageResource(data.userType)
                imgScanQrCode.setOnClickListener {
                    // go to scanner
                }
            }
        }
    }
}