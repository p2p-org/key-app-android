package com.p2p.wowlet.fragment.detailsaving.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemActivityBinding
import com.p2p.wowlet.fragment.detailsaving.viewmodel.DetailSavingViewModel
import com.wowlet.entities.local.ActivityItem

class ActivityDetailAdapter(private val viewModel: DetailSavingViewModel,
                            private val list: List<ActivityItem>): RecyclerView.Adapter<ActivityDetailAdapter.MyViewHolder>() {

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
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(
        val itemActivityBinding: ItemActivityBinding
    ) : RecyclerView.ViewHolder(itemActivityBinding.root)

}