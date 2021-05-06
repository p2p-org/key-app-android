package com.p2p.wallet.main.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.databinding.ItemTokenGroupBinding
import com.p2p.wallet.main.model.TokenItem

class TokenGroupViewHolder(
    binding: ItemTokenGroupBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        ItemTokenGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private val shownView = binding.shownView
    private val hiddenView = binding.hiddenView
    private val groupTextView = binding.groupTextView
    private val hiddenRecyclerView = binding.hiddenRecyclerView

    private val tokenAdapter: TokenHiddenAdapter by lazy {
        TokenHiddenAdapter { }
    }

    private val tokenLayoutManager = LinearLayoutManager(itemView.context)

    fun onBind(group: TokenItem.Group) {
        val tokens = group.hiddenTokens
        groupTextView.text = itemView.context.getString(R.string.main_hidden_wallets, tokens.size)

        with(hiddenRecyclerView) {
            layoutManager = tokenLayoutManager
            adapter = tokenAdapter
        }

        tokenAdapter.setItems(tokens)

        hiddenView.setOnClickListener {
            shownView.isVisible = true
            hiddenRecyclerView.isVisible = true
            hiddenView.isVisible = false
        }

        shownView.setOnClickListener {
            hiddenView.isVisible = true
            hiddenRecyclerView.isVisible = false
            shownView.isVisible = false
        }
    }

    fun onViewRecycled() {
        hiddenRecyclerView.layoutManager = null
        hiddenRecyclerView.adapter = null
    }
}