package org.p2p.wallet.home.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.databinding.ItemBannerSingleBinding
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.home.model.Banner

class SingleBannerViewHolder(
    private val binding: ItemBannerSingleBinding,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val MARGIN_HORIZONTAL = 32
    }

    constructor(
        parent: ViewGroup,
        listener: OnHomeItemsClickListener
    ) : this(
        binding = ItemBannerSingleBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    private val screenWidth: Int

    init {
        val displayMetrics = binding.root.context.resources.displayMetrics
        val pxWidth = displayMetrics.widthPixels
        screenWidth = pxWidth - MARGIN_HORIZONTAL
    }

    fun onBind(item: Banner) {
        with(binding) {
            val width = if (item.isSingle) {
                screenWidth
            } else {
                root.resources.getDimension(R.dimen.min_banner_width).toInt()
            }
            colorView.layoutParams.width = width

            optionsTextView.setText(item.optionTextId)
            actionTextView.setText(item.actionTextId)
            bannerImageView.setImageResource(item.drawableRes)
            colorView.setBackgroundColor(root.getColor(item.backgroundColorRes))

            root.setOnClickListener { listener.onBannerClicked(item.optionTextId) }
        }
    }
}
