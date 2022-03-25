package org.p2p.wallet.history.ui.history.adapter.holders

import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemProgressBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class ProgressViewHolder(
    parent: ViewGroup,
    binding: ItemProgressBinding = parent.inflateViewBinding(attachToRoot = false)
) : HistoryTransactionViewHolder(binding.root)
