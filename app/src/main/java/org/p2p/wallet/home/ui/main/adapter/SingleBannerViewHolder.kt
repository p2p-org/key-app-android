package org.p2p.wallet.home.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemBannerSingleBinding
import org.p2p.wallet.home.model.Banner
import org.p2p.wallet.utils.getColor

class SingleBannerViewHolder(
    private val binding: ItemBannerSingleBinding,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        listener: OnHomeItemsClickListener
    ) : this(
        binding = ItemBannerSingleBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    fun onBind(item: Banner) {
        with(binding) {
            val width = if (item.isSingle) {
                root.resources.getDimension(R.dimen.max_banner_width).toInt()
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