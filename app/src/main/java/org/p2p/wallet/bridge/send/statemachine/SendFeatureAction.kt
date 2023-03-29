package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal
import org.p2p.core.token.Token

sealed interface SendFeatureAction {

    data class InitFeature(
        val initialAmount: BigDecimal? = null,
        val initialToken: Token.Eth,
    ) : SendFeatureAction

    object RefreshFee : SendFeatureAction

    data class NewToken(
        val token: Token.Eth,
    ) : SendFeatureAction

    data class AmountChange(
        val amount: BigDecimal,
    ) : SendFeatureAction

    data class RestoreSelectedToken(val token: Token.Active) : SendFeatureAction

    object SetupInitialToken : SendFeatureAction
}
