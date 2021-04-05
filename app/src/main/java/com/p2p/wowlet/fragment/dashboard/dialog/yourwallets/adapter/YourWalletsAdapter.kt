package com.p2p.wowlet.fragment.dashboard.dialog.yourwallets.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemMyWalletsBinding
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.utils.bindadapter.imageSource
import com.wowlet.entities.local.WalletItem
import java.math.BigDecimal

class YourWalletsAdapter(
    private var list: List<WalletItem>,
    private var viewModel: SendCoinsViewModel,
) : RecyclerView.Adapter<YourWalletsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding = ItemMyWalletsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(it: List<WalletItem>) {
        list = it
        notifyDataSetChanged()
    }

    inner class MyViewHolder(
        itemWalletsBinding: ItemMyWalletsBinding
    ) : RecyclerView.ViewHolder(itemWalletsBinding.root) {

        private val vCoin = itemWalletsBinding.vCoin
        private val vName = itemWalletsBinding.vName
        private val vPrice = itemWalletsBinding.vPrice
        private val vTkns = itemWalletsBinding.vTkns

        fun onBind(item: WalletItem) {
            vCoin.imageSource(item.icon)
            vName.text = item.tokenName
            vPrice.text = itemView.context.getString(R.string.usd, viewModel.roundCurrencyValue(item.price))
            vTkns.text = (BigDecimal(item.amount).stripTrailingZeros().toDouble().toString() + " " + item.tokenSymbol)

            itemView.setOnClickListener { viewModel.selectWalletItem(item) }
        }
    }
}