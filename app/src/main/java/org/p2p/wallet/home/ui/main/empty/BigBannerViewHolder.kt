package org.p2p.wallet.home.ui.main.empty

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemBigBannerBinding
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class BigBannerViewHolder(
    parent: ViewGroup,
    private val binding: ItemBigBannerBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onBannerButtonClicked: (buttonId: Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: HomeBannerItem) = with(binding) {
        textViewBannerTitle.setText(item.titleTextId)

        val subtitleText = context.getString(item.subtitleTextId)
        textViewBannerSubtitle.text = subtitleText
        textViewBannerSubtitle.isVisible = subtitleText.isNotEmpty()

        imageViewBanner.setImageResource(item.drawableRes)

        button.apply {
            setText(item.buttonTextId)
            setOnClickListener { onBannerButtonClicked(item.titleTextId) }
        }
        setBackground(item.backgroundColorRes)
    }

    private fun ItemBigBannerBinding.setBackground(@ColorRes backgroundColorRes: Int) {
        when (val background: Drawable = viewBackground.background) {
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
