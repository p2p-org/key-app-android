package com.p2p.wallet.main.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.databinding.ItemTokenHiddenGroupButtonBinding
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.main.model.VisibilityState

class TokenButtonViewHolder(
    binding: ItemTokenHiddenGroupButtonBinding,
    private val onToggleClicked: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onToggleClicked: () -> Unit
    ) : this(
        binding = ItemTokenHiddenGroupButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onToggleClicked = onToggleClicked
    )

    private val hiddenView = binding.hiddenView
    private val shownView = binding.shownView
    private val groupTextView = binding.groupTextView

    fun onBind(item: TokenItem.Action) {
        when (item.state) {
            is VisibilityState.Hidden -> {
                hiddenView.isVisible = true
                shownView.isVisible = false
                val resources = itemView.context.resources
                groupTextView.text = resources.getQuantityString(
                    R.plurals.hidden_wallets, item.state.count, item.state.count
                )
            }
            VisibilityState.Visible -> {
                hiddenView.isVisible = false
                shownView.isVisible = true
            }
        }

        hiddenView.setOnClickListener { onToggleClicked() }
        shownView.setOnClickListener { onToggleClicked() }
    }
}