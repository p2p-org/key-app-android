package com.p2p.wowlet.fragment.backupwallat.secretkeys.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.ItemSecretKeyBinding
import com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.wowlet.entities.local.SecretKeyItem

class RandomKeyAdapter(private val viewModel: SecretKeyViewModel, private var list: List<SecretKeyItem>) :
    RecyclerView.Adapter<RandomKeyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind: ItemSecretKeyBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_secret_key,
            parent,
            false
        )

        return MyViewHolder(bind)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemSecretKeyBinding.itemSecretKey = list[position]
        holder.itemSecretKeyBinding.viewModel = viewModel
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateData(data: MutableList<SecretKeyItem>) {
        list=data
        notifyDataSetChanged()
    }

    inner class MyViewHolder(
        val itemSecretKeyBinding: ItemSecretKeyBinding
    ) : RecyclerView.ViewHolder(itemSecretKeyBinding.root)

}