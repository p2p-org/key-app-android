package org.p2p.wallet.sell.ui.payload

import androidx.annotation.ColorRes
import android.content.res.Resources
import org.p2p.core.utils.formatTokenForMoonpay
import org.p2p.core.utils.orZero
import org.p2p.wallet.R
import java.math.BigDecimal

sealed class CashOutButtonState private constructor(
    val isEnabled: Boolean,
    @ColorRes val backgroundColor: Int,
    @ColorRes val textColor: Int,
    val buttonText: String
) {
    class MinAmountEntered(resources: Resources, minTokenSellAmount: BigDecimal) : CashOutButtonState(
        isEnabled = false,
        backgroundColor = R.color.bg_rain,
        textColor = R.color.text_mountain,
        buttonText = resources.getString(
            R.string.sell_payload_min_sol_amount,
            minTokenSellAmount.formatTokenForMoonpay()
        )
    )

    class MaxAmountExceeded(resources: Resources, maxTokenSellAmount: BigDecimal) : CashOutButtonState(
        isEnabled = false,
        backgroundColor = R.color.bg_rain,
        textColor = R.color.text_mountain,
        buttonText = resources.getString(
            R.string.sell_payload_max_sol_amount,
            maxTokenSellAmount.orZero().formatTokenForMoonpay()
        )
    )

    class NotEnoughUserTokenError(resources: Resources) : CashOutButtonState(
        isEnabled = false,
        backgroundColor = R.color.bg_rain,
        textColor = R.color.text_mountain,
        buttonText = resources.getString(R.string.sell_payload_not_enough_sol)
    )

    class CashOutAvailable(resources: Resources) : CashOutButtonState(
        isEnabled = true,
        backgroundColor = R.color.bg_night,
        textColor = R.color.text_snow,
        buttonText = resources.getString(R.string.common_cash_out)
    )
}
