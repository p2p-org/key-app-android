package org.p2p.wallet.common.ui.widget.earnwidget

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.p2p.wallet.R
import java.math.BigDecimal

sealed class EarnWidgetState(@ColorRes val backgroundColor: Int = R.color.bg_rain) {
    object Idle : EarnWidgetState(backgroundColor = R.color.bg_cloud)
    object LearnMore : EarnWidgetState(backgroundColor = R.color.bg_lime)
    data class Depositing(@StringRes val buttonTextRes: Int) : EarnWidgetState()
    object DepositFoundsFailed : EarnWidgetState()
    data class Balance(val amount: BigDecimal, val tokenIcons: List<String>) : EarnWidgetState()
    data class Error(
        @StringRes val messageTextRes: Int,
        @StringRes val buttonTextRes: Int
    ) : EarnWidgetState()
}
