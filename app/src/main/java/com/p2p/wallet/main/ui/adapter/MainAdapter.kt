package com.p2p.wallet.main.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.dashboard.model.local.Token

class MainAdapter(
    private val onItemClicked: (Token) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Token>()

    fun setItems(new: List<Token>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MainViewHolder(parent, onItemClicked)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MainViewHolder).onBind(data[position])
    }
}