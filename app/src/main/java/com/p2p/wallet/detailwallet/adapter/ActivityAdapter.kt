package com.p2p.wallet.detailwallet.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.databinding.ItemActivityBinding
import com.p2p.wallet.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wallet.dashboard.model.local.ActivityItem
import com.p2p.wallet.utils.bindadapter.imageSource
import com.p2p.wallet.utils.bindadapter.walletFormat

class ActivityAdapter(
    private var list: List<ActivityItem>,
    val viewModel: DetailWalletViewModel,
    private val onItemClicked: (ActivityItem) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind: ItemActivityBinding = ItemActivityBinding.inflate(
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
        val binding: ItemActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun onBind(item: ActivityItem) {
            with(binding) {
                vName.text = item.name
                vDate.walletFormat(item.from, 4)
                vCoin.imageSource(item.icon)
                vPrice.text = "${item.symbolsPrice}${itemView.context.getString(R.string.us_new, item.price)}"
                vLamport.text = itemView.context.getString(R.string.sol, item.lamports)
                root.setOnClickListener { onItemClicked.invoke(item) }
            }
        }
    }
}