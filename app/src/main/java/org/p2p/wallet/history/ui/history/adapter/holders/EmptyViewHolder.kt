package org.p2p.wallet.history.ui.history.adapter.holders

import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemHistoryEmptyBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class EmptyViewHolder(
    parent: ViewGroup,
    binding: ItemHistoryEmptyBinding = parent.inflateViewBinding(attachToRoot = false)
) : HistoryTransactionViewHolder(binding.root)
