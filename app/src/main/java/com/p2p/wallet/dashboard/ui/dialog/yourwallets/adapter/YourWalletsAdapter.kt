package com.p2p.wallet.dashboard.ui.dialog.yourwallets.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.databinding.ItemMyWalletsBinding
import com.p2p.wallet.dashboard.ui.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.utils.bindadapter.imageSource

class YourWalletsAdapter(
    private var list: List<Token>,
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

    fun updateList(it: List<Token>) {
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

        fun onBind(item: Token) {
            vCoin.imageSource(item.iconUrl)
            vName.text = item.tokenName
            vPrice.text = itemView.context.getString(R.string.usd, viewModel.roundCurrencyValue(item.price.toDouble()))
            vTkns.text = (item.total.stripTrailingZeros().toDouble().toString() + " " + item.tokenSymbol)

            itemView.setOnClickListener { viewModel.selectWalletItem(item) }
        }
    }
}