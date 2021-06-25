package com.p2p.wallet.main.ui.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.token.model.Token

class TokenHiddenAdapter(
    private val isZerosHidden: Boolean,
    private val onItemClicked: (Token) -> Unit,
    private val onDeleteClicked: (Token) -> Unit,
    private val onEditClicked: (Token) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Token>()

    fun setItems(new: List<Token>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TokenHiddenViewHolder(parent, onItemClicked, onEditClicked, onDeleteClicked)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TokenHiddenViewHolder).onBind(data[position], isZerosHidden)
    }
}