package org.p2p.wallet.striga.offramp.models

import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate

sealed class StrigaOffRampRateState {
    object Loading : StrigaOffRampRateState()
    data class Success(
        val rate: StrigaExchangeRate
    ) : StrigaOffRampRateState()

    data class Failure(val throwable: Throwable) : StrigaOffRampRateState()
}
