package org.p2p.wallet.common.ui.widget.earnwidget

import org.p2p.core.glide.GlideManager
import org.p2p.wallet.databinding.ItemDepositTokenBinding

class DepositTokenViewHolder(
    private val binding: ItemDepositTokenBinding,
    private val glideManager: org.p2p.core.glide.GlideManager
) {

    companion object {
        private const val IMAGE_SIZE = 16
    }

    fun bind(tokenUrl: String) = with(binding) {
        glideManager.load(tokenImageView, tokenUrl, IMAGE_SIZE)
    }
}
