package org.p2p.wallet.home.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemTokenHiddenGroupButtonBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.utils.requireContext

class TokenButtonViewHolder(
    binding: ItemTokenHiddenGroupButtonBinding,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        listener: OnHomeItemsClickListener
    ) : this(
        binding = ItemTokenHiddenGroupButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    private val arrowImageView = binding.arrowImageView
    private val titleTextView = binding.titleTextView

    fun onBind(item: HomeElementItem.Action) {
        val title = requireContext().getString(
            if (item.isHiddenTokens) R.string.main_hidden_tokens
            else R.string.main_active_tokens
        )

        titleTextView.text = title
        arrowImageView.isVisible = item.isHiddenTokens

        if (item.isHiddenTokens) {
            val isHidden = item.state is VisibilityState.Hidden
            val rotationValue = if (isHidden) 180f else 0f
            arrowImageView
                .animate()
                .rotation(rotationValue)
                .start()

            itemView.setOnClickListener { listener.onToggleClicked() }
        }
    }
}