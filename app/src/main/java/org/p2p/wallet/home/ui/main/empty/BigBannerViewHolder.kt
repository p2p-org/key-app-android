package org.p2p.wallet.home.ui.main.empty

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemBigBannerBinding
import org.p2p.wallet.home.model.HomeBanner

class BigBannerViewHolder(
    private val binding: ItemBigBannerBinding,
    private val onBannerButtonClicked: (buttonId: Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onBannerButtonClicked: (buttonId: Int) -> Unit
    ) : this(
        binding = ItemBigBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onBannerButtonClicked = onBannerButtonClicked
    )

    fun onBind(item: HomeBanner) = with(binding) {
        textViewBannerTitle.setText(item.titleTextId)
        textViewBannerSubtitle.setText(item.subtitleTextId)

        imageViewBanner.setImageResource(item.drawableRes)

        buttonBanner.apply {
            setText(item.buttonTextId)
            setOnClickListener { onBannerButtonClicked(item.id) }
        }
        setBackground(item.backgroundColorRes)
    }

    private fun ItemBigBannerBinding.setBackground(@ColorRes backgroundColorRes: Int) {
        val background: Drawable = viewBannerBackground.background
        val context = viewBannerBackground.context
        when (background) {
            is ShapeDrawable -> {
                background.paint.color = ContextCompat.getColor(context, backgroundColorRes)
            }
            is GradientDrawable -> {
                background.setColor(ContextCompat.getColor(context, backgroundColorRes))
            }
            is ColorDrawable -> {
                background.color = ContextCompat.getColor(context, backgroundColorRes)
            }
        }
    }
}
