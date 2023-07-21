package org.p2p.wallet.striga.offramp.mappers

import java.math.BigDecimal
import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.isZero
import org.p2p.wallet.jupiter.model.SwapRateTickerState
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState

class StrigaOffRampMapper {

    private companion object {
        private val MIN_EUR_AMOUNT = BigDecimal("10")
        private val MAX_EUR_AMOUNT = BigDecimal("15000")
    }

    fun mapRateLoading(): SwapRateTickerState = SwapRateTickerState.Loading
    fun mapRateShown(rate: StrigaExchangeRate) = SwapRateTickerState.Shown(rate.formatRate())
    fun mapRateError() = SwapRateTickerState.Hidden

    fun mapButtonStateByAmountsAndTotalBalance(
        amountA: BigDecimal,
        amountB: BigDecimal,
        totalBalance: BigDecimal
    ): StrigaOffRampButtonState {
        return when {
            amountA.isZero() -> StrigaOffRampButtonState.EnterAmount
            amountB < MIN_EUR_AMOUNT -> StrigaOffRampButtonState.ErrorMinLimit
            amountB > MAX_EUR_AMOUNT -> StrigaOffRampButtonState.ErrorMaxLimit
            amountA > totalBalance -> StrigaOffRampButtonState.ErrorInsufficientFunds
            else -> StrigaOffRampButtonState.Enabled
        }
    }

    private fun StrigaExchangeRate.formatRate(): String {
        return "1 ${Constants.EUR_SYMBOL} ≈ ${sellRate.formatFiat()} ${Constants.USDC_SYMBOL}"
    }
}
