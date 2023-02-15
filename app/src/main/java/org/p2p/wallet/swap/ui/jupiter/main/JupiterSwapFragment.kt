package org.p2p.wallet.swap.ui.jupiter.main

import android.os.Bundle
import android.view.View
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.drawable.shape.rippleForeground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.text.TextViewCellModel
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
            SwapWidgetModel(
                isStatic = false,
                widgetTitle = TextViewCellModel(
                    text = TextContainer("You pay")
                ),
                availableAmount = TextViewCellModel(
                    text = TextContainer("3600.284564646 USDC")
                ),
                amountName = TextViewCellModel(
                    text = TextContainer("USDC")
                ),
                balance = TextViewCellModel(
                    text = TextContainer("Balance 3600.28")
                ),
                fiatAmount = TextViewCellModel(
                    text = TextContainer("≈2 USD")
                ),
            )
        )

        binding.swapWidgetTo.bind(
            SwapWidgetModel(
                isStatic = true,
                widgetTitle = TextViewCellModel(
                    text = TextContainer("You receive")
                ),
                amountName = TextViewCellModel(
                    text = TextContainer("SOL")
                ),
                balance = TextViewCellModel(
                    text = TextContainer("Balance 4.4535")
                ),
                fiatAmount = TextViewCellModel(
                    text = TextContainer("≈1984 USD")
                ),
            )
        )
        binding.imageViewSwapTokens.background = shapeDrawable(shapeCircle())
        binding.imageViewSwapTokens.backgroundTintList = view.context.getColorStateList(R.color.button_rain)
        binding.imageViewSwapTokens.rippleForeground(shapeCircle())
    }
}
