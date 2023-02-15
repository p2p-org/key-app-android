package org.p2p.wallet.swap.ui.jupiter.main.widget

import org.p2p.uikit.utils.text.TextViewCellModel

data class SwapWidgetModel(
    val isStatic: Boolean = false,
    val widgetTitle: TextViewCellModel? = null,
    val availableAmount: TextViewCellModel? = null,
    val amountName: TextViewCellModel? = null,
    val amount: TextViewCellModel? = null,
    val balance: TextViewCellModel? = null,
    val fiatAmount: TextViewCellModel? = null,
)
