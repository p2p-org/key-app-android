package org.p2p.wallet.swap.ui.jupiter.main

import androidx.core.view.isInvisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapBinding
import org.p2p.wallet.swap.ui.jupiter.main.widget.SwapWidgetModel
import org.p2p.wallet.utils.viewbinding.viewBinding

class JupiterSwapFragment :
    BaseMvpFragment<JupiterSwapContract.View, JupiterSwapContract.Presenter>(R.layout.fragment_jupiter_swap),
    JupiterSwapContract.View {

    private val binding: FragmentJupiterSwapBinding by viewBinding()
    override val presenter: JupiterSwapContract.Presenter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageViewSwapTokens.background = shapeDrawable(shapeCircle())
        binding.imageViewSwapTokens.backgroundTintList = view.context.getColorStateList(R.color.button_rain)
        binding.imageViewSwapTokens.rippleForeground(shapeCircle())
        binding.imageViewSwapTokens.setOnClickListener {
            presenter.switchTokens()
        }
    }

    override fun setFirstTokenWidgetState(state: SwapWidgetModel) {
        binding.swapWidgetFrom.bind(state)
    }

    override fun setSecondTokenWidgetState(state: SwapWidgetModel) {
        binding.swapWidgetTo.bind(state)
    }

    override fun setButtonState(buttonState: SwapButtonState) = with(binding) {
        when (buttonState) {
            is SwapButtonState.Disabled -> {
                buttonError.isInvisible = false
                sliderSend.isInvisible = true
                buttonState.text.applyTo(buttonError)
            }
            SwapButtonState.Hide -> {
                buttonError.isInvisible = true
                sliderSend.isInvisible = true
            }
            is SwapButtonState.ReadyToSwap -> {
                buttonError.isInvisible = true
                sliderSend.isInvisible = false
                sliderSend.setActionText(buttonState.text)
            }
        }
    }

    override fun setRatioState(state: TextViewCellModel) {
        binding.textViewRate.bind(state)
    }

    private fun setYellowAlert() {
        val context = binding.root.context
        DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(8f.toPx())),
            tint = R.color.bg_light_sun,
            strokeWidth = 1f.toPx(),
            strokeColor = R.color.bg_sun,
        ).applyBackground(binding.linearLayoutAlert)
        binding.imageViewAlert.imageTintList = context.getColorStateList(R.color.icons_sun)
        binding.textViewAlert.setTextColor(context.getColorStateList(R.color.text_night))
    }

    private fun setRoseAlert() {
        val context = binding.root.context
        DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(8f.toPx())),
            tint = R.color.light_rose,
            strokeWidth = 1f.toPx(),
            strokeColor = R.color.bg_rose,
        ).applyBackground(binding.linearLayoutAlert)
        binding.imageViewAlert.imageTintList = context.getColorStateList(R.color.icons_rose)
        binding.textViewAlert.setTextColor(context.getColorStateList(R.color.text_rose))
    }
}
