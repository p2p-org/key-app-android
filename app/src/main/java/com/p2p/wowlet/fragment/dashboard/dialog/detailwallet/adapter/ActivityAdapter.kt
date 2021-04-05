package com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemActivityBinding
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.utils.bindadapter.imageSource
import com.wowlet.entities.local.ActivityItem

class ActivityAdapter(
    private var list: List<ActivityItem>,
    val viewModel: DetailWalletViewModel,
) : RecyclerView.Adapter<ActivityAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind = ItemActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(bind)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(it: List<ActivityItem>) {
        list = it
        notifyDataSetChanged()
    }

    inner class MyViewHolder(
        private val itemActivityBinding: ItemActivityBinding
    ) : RecyclerView.ViewHolder(itemActivityBinding.root) {

        private val vCoin = itemActivityBinding.vCoin
        private val vName = itemActivityBinding.vName
        private val vDate = itemActivityBinding.vDate
        private val vPrice = itemActivityBinding.vPrice
        private val vLamport = itemActivityBinding.vLamport

        fun onBind(item: ActivityItem) {
            vDate.text = item.from
            vName.text = item.name
            val price = "${item.symbolsPrice} ${itemView.context.getString(R.string.us_new, item.price)}"
            vPrice.text = price
            vLamport.text = itemView.context.getString(R.string.sol, item.lamports)
            vCoin.imageSource(item.icon)
            itemView.setOnClickListener { viewModel.openTransactionDialog(item) }
        }
    }
}