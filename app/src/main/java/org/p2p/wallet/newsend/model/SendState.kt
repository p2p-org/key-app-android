package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee

sealed interface SendState {

    data class FeePayerUpdated(val newFeePayer: Token.Active, val fee: FeeRelayerFee) : SendState

    object FreeTransaction : SendState

    object Loading : SendState

    data class Failed(val e: Throwable) : SendState
}
