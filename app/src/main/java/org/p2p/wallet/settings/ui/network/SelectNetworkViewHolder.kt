package org.p2p.wallet.settings.ui.network

import android.view.ViewGroup
import androidx.core.view.isVisible
import org.p2p.wallet.common.ui.recycler.adapter.BaseSelectionViewHolder
import org.p2p.wallet.databinding.ItemSelectNetworkBinding
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SelectNetworkViewHolder(
    parent: ViewGroup,
    private val binding: ItemSelectNetworkBinding = parent.inflateViewBinding(attachToRoot = false),
    onItemClicked: (NetworkEnvironment) -> Unit
) : BaseSelectionViewHolder<NetworkEnvironment>(binding.root, onItemClicked) {

    override fun onBind(item: NetworkEnvironment, selectedItem: NetworkEnvironment?) {
        super.onBind(item, selectedItem)
        with(binding) {
            viewDivider.isVisible = bindingAdapterPosition != 0
            imageViewCheck.isVisible = item == selectedItem

            textViewNetwork.text = item.endpoint
        }
    }
}
