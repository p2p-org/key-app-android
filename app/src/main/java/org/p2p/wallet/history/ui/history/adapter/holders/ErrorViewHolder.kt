package org.p2p.wallet.history.ui.history.adapter.holders

import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.databinding.ItemErrorBinding
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class ErrorViewHolder(
    parent: ViewGroup,
    private val binding: ItemErrorBinding = parent.inflateViewBinding(attachToRoot = false)
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(pagingState: PagingState, onRetry: () -> Unit) {
        val errorMessage = if (pagingState is PagingState.Error) {
            pagingState.error.message.orEmpty()
        } else {
            binding.getString(R.string.error_general_message)
        }

        binding.error.text = errorMessage
        binding.retryButton.setOnClickListener { onRetry() }
    }
}
