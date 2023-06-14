package org.p2p.wallet.home.ui.main.adapter

import androidx.core.view.isVisible
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemHomeBannerBinding
import org.p2p.wallet.home.model.HomeScreenBanner
import org.p2p.wallet.kyc.model.StrigaBanner
import org.p2p.wallet.utils.viewbinding.context

class SingleBannerViewHolder(
    private val binding: ItemHomeBannerBinding,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val MARGIN_HORIZONTAL = 32
    }

    constructor(
        parent: ViewGroup,
        listener: OnHomeItemsClickListener
    ) : this(
        binding = ItemHomeBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

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
        val item = item.status
        textViewTitle.setText(item.bannerTitleResId)

        val subtitleText = context.getString(item.bannerMessageResId)
        textViewSubtitle.text = subtitleText
        textViewSubtitle.isVisible = subtitleText.isNotEmpty()

        textViewSubtitle.setText(item.bannerMessageResId)
        imageViewIcon.setImageResource(item.placeholderResId)
        buttonAction.setText(item.actionTitleResId)
        root.background.setTint(context.getColor(item.backgroundTint))

        buttonClose.isVisible = item.isCloseButtonVisible

        buttonAction.setOnClickListener {
            listener.onBannerClicked(item.bannerTitleResId)
        }
        buttonClose.setOnClickListener {
            listener.onBannerCloseClicked(item.bannerTitleResId)
        }
    }
}
