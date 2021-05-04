package com.p2p.wallet.dashboard.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.dashboard.ui.viewmodel.DashboardViewModel
import com.p2p.wallet.databinding.ItemTokenBinding

class WalletsAdapter(
    private val viewModel: DashboardViewModel,
    private var list: List<Token>,
    private val onItemClicked: () -> Unit
) : RecyclerView.Adapter<WalletsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind = ItemTokenBinding.inflate(
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

    fun setData(walletList: List<Token>) {
        list = walletList
        notifyDataSetChanged()
    }

    fun setItemData(walletItem: Token) {
        list.find { item ->
            if (item.depositAddress == walletItem.depositAddress) {
//                item.tokenName = walletItem.tokenName
                true
            } else
                false
        }
        notifyDataSetChanged()
    }

    inner class MyViewHolder(
        val binding: ItemTokenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

//        private val itemWalletView = binding.itemWalletView
//        private val vCoin = binding.vCoin
//        private val vItem = binding.vItem
//        private val vWalletAddress = binding.vWalletAddress
//        private val vPrice = binding.vPrice
//        private val vTkns = binding.vTkns

        @SuppressLint("SetTextI18n")
        fun onBind(item: Token) {
//            vWalletAddress.walletFormat(item.depositAddress, 4)
//            vItem.text = item.tokenName
//            vCoin.imageSource(item.icon)
//            vPrice.text = "${itemView.context.getString(R.string.usd_symbol)}${item.price.roundCurrencyValue()}"
//            vTkns.text = "${item.amount} ${item.tokenSymbol}}"
//            itemWalletView.setOnClickListener {
//                onItemClicked()
//            }
        }
    }
}