package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal
import org.p2p.wallet.bridge.send.statemachine.model.SendToken

sealed interface SendFeatureAction {

    object InitFeature : SendFeatureAction

    data class RefreshFee(
        val amount: BigDecimal?
    ) : SendFeatureAction

    data class NewToken(
        val token: SendToken
    ) : SendFeatureAction

    data class AmountChange(
        val amount: BigDecimal
    ) : SendFeatureAction

    object ZeroAmount : SendFeatureAction
    object MaxAmount : SendFeatureAction
}
