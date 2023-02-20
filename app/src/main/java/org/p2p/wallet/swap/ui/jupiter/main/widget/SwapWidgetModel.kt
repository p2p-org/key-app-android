package org.p2p.wallet.swap.ui.jupiter.main.widget

import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx

sealed interface SwapWidgetModel {
    data class Content(
        val isStatic: Boolean = false,
        val widgetTitle: TextViewCellModel? = null,
        val availableAmount: TextViewCellModel? = null,
        val currencyName: TextViewCellModel? = null,
        val amount: TextViewCellModel? = null,
        val amountMaxDecimals: Int? = null,
        val balance: TextViewCellModel? = null,
        val fiatAmount: TextViewCellModel? = null,
    ) : SwapWidgetModel

    data class Loading(
        val isStatic: Boolean = false,
        val widgetTitle: TextViewCellModel.Raw,
        val currencySkeleton: TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(bigSkeleton()),
        val amountSkeleton: TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(bigSkeleton()),
        val balanceSkeleton: TextViewCellModel.Skeleton = TextViewCellModel.Skeleton(smallSkeleton()),
    ) : SwapWidgetModel
}

fun bigSkeleton(): SkeletonCellModel = SkeletonCellModel(
    height = 20.toPx(),
    width = 84.toPx(),
    radius = 6f.toPx(),
)

fun smallSkeleton(): SkeletonCellModel = SkeletonCellModel(
    height = 8.toPx(),
    width = 84.toPx(),
    radius = 2f.toPx(),
)
