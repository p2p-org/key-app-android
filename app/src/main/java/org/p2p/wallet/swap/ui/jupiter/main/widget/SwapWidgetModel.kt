package org.p2p.wallet.swap.ui.jupiter.main.widget

import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R

sealed interface SwapWidgetModel {
    data class Content(
        val isStatic: Boolean = false,
        val widgetTitle: TextViewCellModel? = null,
        val availableAmount: TextViewCellModel? = null,
        val currencyName: TextViewCellModel? = null,
        val amount: TextViewCellModel? = null,
        val balance: TextViewCellModel? = null,
        val fiatAmount: TextViewCellModel? = null,
    ) : SwapWidgetModel {
        companion object {
            fun swapToAmountLoading(
                tokenName: String,
                userBalance: String? = null,
            ): Content =
                Content(
                    isStatic = true,
                    widgetTitle = TextViewCellModel.Raw(TextContainer(R.string.swap_main_you_receive)),
                    currencyName = TextViewCellModel.Raw(TextContainer(tokenName)),
                    amount = TextViewCellModel.Skeleton(bigSkeleton()),
                    balance = userBalance
                        ?.let { TextViewCellModel.Raw(TextContainer(R.string.swap_main_balance_amount, listOf(it))) },
                    fiatAmount = null,
                    availableAmount = null,
                )
        }
    }

    data class Loading(
        val isStatic: Boolean = false,
        val widgetTitle: TextViewCellModel.Raw,
        val currencySkeleton: TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(bigSkeleton()),
        val amountSkeleton: TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(bigSkeleton()),
        val balanceSkeleton: TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(smallSkeleton()),
    ) : SwapWidgetModel
}

private fun bigSkeleton(): SkeletonCellModel = SkeletonCellModel(
    height = 20.toPx(),
    width = 84.toPx(),
    radius = 6f.toPx(),
)

private fun smallSkeleton(): SkeletonCellModel = SkeletonCellModel(
    height = 8.toPx(),
    width = 84.toPx(),
    radius = 2f.toPx(),
)
