package org.p2p.wallet.sell.ui.payload

import androidx.annotation.ColorRes
import android.content.res.Resources
import org.p2p.core.utils.formatTokenForMoonpay
import org.p2p.core.utils.orZero
import org.p2p.wallet.R
import java.math.BigDecimal

class CashOutButtonState private constructor(
    val isEnabled: Boolean,
    @ColorRes val backgroundColor: Int,
    @ColorRes val textColor: Int,
    val buttonText: String
) {
    class Builder(private val resources: Resources) {
        fun minAmountErrorState(minTokenSellAmount: BigDecimal): CashOutButtonState =
            CashOutButtonState(
                isEnabled = false,
                backgroundColor = R.color.bg_rain,
                textColor = R.color.text_mountain,
                buttonText = resources.getString(
                    R.string.sell_payload_min_sol_amount,
                    minTokenSellAmount.formatTokenForMoonpay()
                )
            )

        fun maxAmountErrorState(maxTokenSellAmount: BigDecimal): CashOutButtonState =
            CashOutButtonState(
                isEnabled = false,
                backgroundColor = R.color.bg_rain,
                textColor = R.color.text_mountain,
                buttonText = resources.getString(
                    R.string.sell_payload_max_sol_amount,
                    maxTokenSellAmount.orZero().formatTokenForMoonpay()
                )
            )

        fun notEnoughTokenErrorState(): CashOutButtonState =
            CashOutButtonState(
                isEnabled = false,
                backgroundColor = R.color.bg_rain,
                textColor = R.color.text_mountain,
                buttonText = resources.getString(R.string.sell_payload_not_enough_sol)
            )

        fun cashOutAvailableState(): CashOutButtonState =
            CashOutButtonState(
                isEnabled = true,
                backgroundColor = R.color.bg_night,
                textColor = R.color.text_snow,
                buttonText = resources.getString(R.string.common_cash_out)
            )
    }
}
