package org.p2p.wallet.striga.offramp.ui

import org.p2p.wallet.jupiter.model.SwapRateTickerState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState

data class StrigaOffRampViewState(
    val exchangeRateState: SwapRateTickerState,
    val buttonState: StrigaOffRampButtonState
)
