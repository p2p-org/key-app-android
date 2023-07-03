package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.p2p.uikit.utils.requireContext
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemTokenGroupButtonBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.VisibilityState

class TokenButtonViewHolder(
    private val binding: ItemTokenGroupButtonBinding,
    private val listener: HomeItemsClickListeners
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        listener: HomeItemsClickListeners
    ) : this(
        binding = ItemTokenGroupButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    fun onBind(item: HomeElementItem.Action) = with(binding) {
        val title = requireContext().getString(R.string.main_hidden_tokens)
        textViewTitle.text = title
        bindIcon(item)
        itemView.setOnClickListener { listener.onToggleClicked() }
    }

    fun bindIcon(item: HomeElementItem.Action) {
        val isHidden = item.state is VisibilityState.Hidden
        val iconResId = if (isHidden) R.drawable.ic_token_expose else R.drawable.ic_token_hide
        binding.imageViewTokenState.setImageResource(iconResId)
    }
}
