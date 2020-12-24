package com.p2p.wowlet.fragment.dashboard.dialog.addcoin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemAddCoinBinding
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.utils.copyClipboard
import com.wowlet.entities.local.AddCoinItem

class AddCoinAdapter(
    private val list: List<AddCoinItem>,
    private val dashboardViewModel: DashboardViewModel
) : RecyclerView.Adapter<AddCoinAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind: ItemAddCoinBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_add_coin,
            parent,
            false
        )
        return MyViewHolder(bind)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemAddCoinBinding.itemAddCoin = list[position]
        holder.itemAddCoinBinding.viewModel = dashboardViewModel
        holder.itemAddCoinBinding.btCopyMint.setOnClickListener {
            it.context.copyClipboard(list[position].mintAddress)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(
        val itemAddCoinBinding: ItemAddCoinBinding
    ) : RecyclerView.ViewHolder(itemAddCoinBinding.root)
}