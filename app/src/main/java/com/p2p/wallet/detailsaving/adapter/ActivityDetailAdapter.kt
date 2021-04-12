package com.p2p.wallet.detailsaving.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.databinding.ItemActivityBinding
import com.p2p.wallet.detailsaving.viewmodel.DetailSavingViewModel
import com.p2p.wallet.dashboard.model.local.ActivityItem

class ActivityDetailAdapter(
    private val viewModel: DetailSavingViewModel,
    private val list: List<ActivityItem>
) : RecyclerView.Adapter<ActivityDetailAdapter.MyViewHolder>() {

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

    inner class MyViewHolder(
        private val binding: ItemActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: ActivityItem) {
            with(binding) {
                vName.text = item.name
                vDate.text = item.from
            }
        }
    }
}