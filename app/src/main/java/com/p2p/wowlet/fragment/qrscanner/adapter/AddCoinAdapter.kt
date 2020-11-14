package com.p2p.wowlet.fragment.qrscanner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemAddCoinBinding
import com.p2p.wowlet.fragment.qrscanner.viewmodel.QrScannerViewModel
import com.wowlet.entities.local.AddCoinItem

class AddCoinAdapter(
    private val viewModel: QrScannerViewModel,
    private val list: List<AddCoinItem>
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
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(
        val itemAddCoinBinding: ItemAddCoinBinding
    ) : RecyclerView.ViewHolder(itemAddCoinBinding.root)
}