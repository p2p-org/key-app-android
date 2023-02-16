package org.p2p.wallet.swap.ui.jupiter.main

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindSkeleton
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapBinding
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel
import org.p2p.wallet.utils.viewbinding.viewBinding

class JupiterSwapFragment : BaseFragment(R.layout.fragment_jupiter_swap) {

    private val binding: FragmentJupiterSwapBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swapWidgetFrom.bind(
            SwapWidgetModel.Loading(
                isStatic = false,
                widgetTitle = TextViewCellModel.Raw(text = TextContainer(R.string.swap_main_you_pay)),
            )
        )

        binding.swapWidgetTo.bind(
            SwapWidgetModel.Loading(
                isStatic = true,
                widgetTitle = TextViewCellModel.Raw(text = TextContainer(R.string.swap_main_you_receive)),
            )
        )
        binding.textViewRate.bindSkeleton(
            TextViewCellModel.Skeleton(
                SkeletonCellModel(
                    height = 16.toPx(),
                    width = 160.toPx(),
                    radius = 4f.toPx(),
                )
            )
        )

        binding.imageViewSwapTokens.background = shapeDrawable(shapeCircle())
        binding.imageViewSwapTokens.backgroundTintList = view.context.getColorStateList(R.color.button_rain)
        binding.imageViewSwapTokens.rippleForeground(shapeCircle())
        binding.imageViewSwapTokens.setOnClickListener { }
        setYellowAller()
        binding.linearLayoutAlert.isVisible = true
    }

    private fun setYellowAller() {
        val context = binding.root.context
        DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(8f.toPx())),
            tint = R.color.bg_light_sun,
            strokeWidth = 1f.toPx(),
            strokeColor = R.color.bg_sun,
        ).applyBackground(binding.linearLayoutAlert)
        binding.imageviewAlert.imageTintList = context.getColorStateList(R.color.icons_sun)
        binding.textViewAlert.setTextColor(context.getColorStateList(R.color.text_night))
    }

    private fun setRoseAller() {
        val context = binding.root.context
        DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(8f.toPx())),
            tint = R.color.light_rose,
            strokeWidth = 1f.toPx(),
            strokeColor = R.color.bg_rose,
        ).applyBackground(binding.linearLayoutAlert)
        binding.imageviewAlert.imageTintList = context.getColorStateList(R.color.icons_rose)
        binding.textViewAlert.setTextColor(context.getColorStateList(R.color.text_rose))
    }
}
