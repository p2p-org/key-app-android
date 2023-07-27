package org.p2p.wallet.striga.offramp.interactor

import java.math.BigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.utils.orZero
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate
import org.p2p.wallet.striga.offramp.interactor.polling.StrigaOffRampExchangeRateNotifier
import org.p2p.wallet.striga.offramp.models.StrigaOffRampRateState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenType
import org.p2p.core.utils.divideSafe

class StrigaOffRampInteractor(
    private val exchangeRateNotifier: StrigaOffRampExchangeRateNotifier,
) {
    fun observeExchangeRateState(): StateFlow<StrigaOffRampRateState> = exchangeRateNotifier.observeExchangeRateState()

    fun startExchangeRateNotifier(scope: CoroutineScope) {
        exchangeRateNotifier.start(scope)
    }

    fun stopExchangeRateNotifier() {
        exchangeRateNotifier.stop()
    }

    fun calculateAmountByRate(
        tokenType: StrigaOffRampTokenType,
        rate: StrigaExchangeRate?,
        amount: BigDecimal
    ): BigDecimal {
        return when (tokenType) {
            StrigaOffRampTokenType.TokenA -> {
                amount.divideSafe(rate?.sellRate.orZero())
            }
            StrigaOffRampTokenType.TokenB -> {
                amount * rate?.sellRate.orZero()
            }
        }
    }
}
