package org.p2p.wallet.send.ui.search.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.uikit.atoms.skeleton.UiKitSkeletonLineModel
import org.p2p.uikit.databinding.ItemAtomSkeletonLineViewBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SearchSkeletonViewHolder(
    parent: ViewGroup,
    private val binding: ItemAtomSkeletonLineViewBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: UiKitSkeletonLineModel) {
        binding.root.bind(item)
    }
}
