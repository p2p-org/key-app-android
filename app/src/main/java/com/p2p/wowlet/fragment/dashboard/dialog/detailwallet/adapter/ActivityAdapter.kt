package com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemActivityBinding
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.wowlet.entities.local.ActivityItem

class ActivityAdapter(
    private var list: List<ActivityItem>,
    val viewModel: DetailWalletViewModel,
) : RecyclerView.Adapter<ActivityAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind: ItemActivityBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_activity,
            parent,
            false
        )
        return MyViewHolder(bind)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemActivityBinding.itemActivity = list[position]
        holder.itemActivityBinding.viewModel = viewModel
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(it: List<ActivityItem>) {
        list = it
        notifyDataSetChanged()
    }

    inner class MyViewHolder(
        val itemActivityBinding: ItemActivityBinding
    ) : RecyclerView.ViewHolder(itemActivityBinding.root)

}