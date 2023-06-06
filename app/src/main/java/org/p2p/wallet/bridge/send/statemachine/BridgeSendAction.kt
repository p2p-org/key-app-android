package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

sealed interface BridgeSendAction {

    object InitFeature : BridgeSendAction

    data class RefreshFee(
        val amount: BigDecimal?
    ) : BridgeSendAction

    data class NewToken(
        val token: SendToken
    ) : BridgeSendAction

    data class AmountChange(
        val amount: BigDecimal
    ) : BridgeSendAction

    object ZeroAmount : BridgeSendAction
    object MaxAmount : BridgeSendAction
}
