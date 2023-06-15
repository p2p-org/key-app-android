package org.p2p.wallet.home.ui.main.adapter

import androidx.core.view.isVisible
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemHomeBannerBinding
import org.p2p.wallet.home.model.HomeScreenBanner
import org.p2p.wallet.kyc.model.StrigaBanner
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SingleBannerViewHolder(
    parent: ViewGroup,
    private val binding: ItemHomeBannerBinding = parent.inflateViewBinding(),
    private val listener: OnHomeItemsClickListener
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

        val subtitleText = context.getString(status.bannerMessageResId)
        textViewSubtitle.text = subtitleText
        textViewSubtitle.isVisible = subtitleText.isNotEmpty()

        textViewSubtitle.setText(status.bannerMessageResId)
        imageViewIcon.setImageResource(status.placeholderResId)
        buttonAction.setText(status.actionTitleResId)
        root.background.setTint(context.getColor(status.backgroundTint))

        buttonClose.isVisible = status.isCloseButtonVisible

        buttonAction.setOnClickListener {
            listener.onBannerClicked(status.bannerTitleResId)
        }
        buttonClose.setOnClickListener {
            listener.onBannerCloseClicked(status.bannerTitleResId)
        }
    }
}
