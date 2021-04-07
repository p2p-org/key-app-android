package com.p2p.wowlet.dialog.sendcoins.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.entities.local.UserWalletType
import kotlinx.android.synthetic.main.item_send_coin_type.view.*

class PagerAdapter(
    private val list: List<UserWalletType>,
    private val viewModel: SendCoinsViewModel
) : RecyclerView.Adapter<PagerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_send_coin_type, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindView(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(data: UserWalletType) {
            with(itemView) {
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