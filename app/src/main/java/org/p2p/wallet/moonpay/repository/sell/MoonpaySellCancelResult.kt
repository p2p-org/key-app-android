package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.moonpay.model.MoonpaySellError

sealed interface MoonpaySellCancelResult {
    object CancelSuccess : MoonpaySellCancelResult
    data class CancelFailed(val cause: MoonpaySellError) : MoonpaySellCancelResult
}
