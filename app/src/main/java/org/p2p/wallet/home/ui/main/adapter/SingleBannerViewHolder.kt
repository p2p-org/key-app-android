package org.p2p.wallet.home.ui.main.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.ItemHomeBannerBinding
import org.p2p.wallet.home.model.HomeScreenBanner
import org.p2p.wallet.kyc.model.StrigaBanner
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SingleBannerViewHolder(
    parent: ViewGroup,

    private val listener: HomeItemsClickListeners,
    private val binding: ItemHomeBannerBinding = parent.inflateViewBinding(attachToRoot = false)
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val MARGIN_HORIZONTAL = 32
    }

    private val screenWidth: Int

    init {
        val displayMetrics = binding.root.context.resources.displayMetrics
        val pxWidth = displayMetrics.widthPixels
        screenWidth = pxWidth - MARGIN_HORIZONTAL
    }

    fun onBind(item: HomeScreenBanner) {
        when (item) {
            is StrigaBanner -> onBind(item)
        }
    }

    private fun onBind(item: StrigaBanner) = with(binding) {
        val status = item.status
        textViewTitle.setText(status.bannerTitleResId)

        val subtitleText = getString(status.bannerMessageResId)
        textViewSubtitle.text = subtitleText
        textViewSubtitle.isVisible = subtitleText.isNotEmpty()

        imageViewIcon.setImageResource(status.placeholderResId)
        buttonAction.setText(status.actionTitleResId)
        root.background.setTint(getColor(status.backgroundTint))

        buttonClose.isVisible = status.isCloseButtonVisible

        buttonAction.setLoading(item.isLoading)
        buttonAction.setOnClickListener {
            listener.onBannerClicked(status.bannerTitleResId)
        }
        buttonClose.setOnClickListener {
            listener.onBannerCloseClicked(status.bannerTitleResId)
        }
    }
}
