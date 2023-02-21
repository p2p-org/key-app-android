package org.p2p.wallet.swap.ui.jupiter.main.widget

import org.p2p.uikit.utils.skeleton.textCellSkeleton
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
        val currencySkeleton: TextViewCellModel.Skeleton = textCellSkeleton(
            height = 20.toPx(),
            width = 84.toPx(),
            radius = 6f.toPx(),
        ),
        val amountSkeleton: TextViewCellModel.Skeleton = textCellSkeleton(
            height = 20.toPx(),
            width = 84.toPx(),
            radius = 6f.toPx(),
        ),
        val balanceSkeleton: TextViewCellModel.Skeleton = textCellSkeleton(
            height = 8.toPx(),
            width = 84.toPx(),
            radius = 2f.toPx(),
        ),
    ) : SwapWidgetModel
}
