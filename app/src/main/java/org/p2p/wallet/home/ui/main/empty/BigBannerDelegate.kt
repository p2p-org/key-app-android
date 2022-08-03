package org.p2p.wallet.home.ui.main.empty

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import androidx.annotation.ColorRes
import org.p2p.wallet.common.delegates.SmartDelegate
import org.p2p.wallet.databinding.ItemBigBannerBinding
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class BigBannerDelegate(
    private val onBannerButtonClicked: (buttonId: Int) -> Unit,
) : SmartDelegate<HomeBannerItem, ItemBigBannerBinding>(
    { parent -> parent.inflateViewBinding(attachToRoot = false) }
) {

    override fun onBindViewHolder(
        holder: ViewHolder<ItemBigBannerBinding>,
        data: HomeBannerItem
    ) = with(holder.binding) {
        textViewBannerTitle.setText(data.titleTextId)
        textViewBannerSubtitle.setText(data.subtitleTextId)

        imageViewBanner.setImageResource(data.drawableRes)

        buttonBanner.apply {
            setText(data.buttonTextId)
            setOnClickListener { onBannerButtonClicked(data.id) }
        }
        setBackground(data.backgroundColorRes)
    }

    private fun ItemBigBannerBinding.setBackground(@ColorRes backgroundColorRes: Int) {
        when (val background: Drawable = viewBannerBackground.background) {
            is ShapeDrawable -> {
                background.paint.color = getColor(backgroundColorRes)
            }
            is GradientDrawable -> {
                background.setColor(getColor(backgroundColorRes))
            }
            is ColorDrawable -> {
                background.color = getColor(backgroundColorRes)
            }
        }
    }
}
