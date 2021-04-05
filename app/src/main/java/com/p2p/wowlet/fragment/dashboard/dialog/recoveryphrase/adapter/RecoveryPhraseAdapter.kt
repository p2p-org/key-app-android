package com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wowlet.databinding.ItemRecoveryPhraseBinding
import com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel

class RecoveryPhraseAdapter(
    private val viewModel: RecoveryPhraseViewModel,
    private val list: List<String>
) : RecyclerView.Adapter<RecoveryPhraseAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val bind = ItemRecoveryPhraseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MyViewHolder(bind)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemRecoveryPhraseBinding.vItem.text = "${(position + 1)}.${list[position]}"
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(
        val itemRecoveryPhraseBinding: ItemRecoveryPhraseBinding
    ) : RecyclerView.ViewHolder(itemRecoveryPhraseBinding.root)
}