package org.p2p.wallet.history.ui.token.adapter.holders

import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemErrorBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class ErrorViewHolder(
    parent: ViewGroup,
    private val binding: ItemErrorBinding = parent.inflateViewBinding(attachToRoot = false)
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(onRetry: () -> Unit) {
        binding.buttonRetry.setOnClickListener { onRetry() }
    }
}
