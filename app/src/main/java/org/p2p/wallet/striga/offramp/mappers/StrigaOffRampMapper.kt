package org.p2p.wallet.striga.offramp.mappers

import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatFiat
import org.p2p.wallet.jupiter.model.SwapRateTickerState
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate

class StrigaOffRampMapper {

    fun mapRateLoading(): SwapRateTickerState = SwapRateTickerState.Loading
    fun mapRateShown(rate: StrigaExchangeRate) = SwapRateTickerState.Shown(rate.formatRate())
    fun mapRateError() = SwapRateTickerState.Hidden

    private fun StrigaExchangeRate.formatRate(): String {
        return "1 ${Constants.EUR_SYMBOL} ≈ ${buyRate.formatFiat()} ${Constants.USDC_SYMBOL}"
    }
}
