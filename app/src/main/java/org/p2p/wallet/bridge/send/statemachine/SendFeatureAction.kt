package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.wrapper.eth.EthAddress

sealed interface SendFeatureAction {

    data class InitFeature(
        val initialAmount: BigDecimal? = null,
        val initialToken: Token.Eth,
    ) : SendFeatureAction

    data class RefreshFee(
        val userWallet: SolAddress,
        val recipient: EthAddress,
        val mintAddress: SolAddress?,
        val amount: String
    ) : SendFeatureAction

    data class NewToken(
        val token: Token.Eth,
    ) : SendFeatureAction

    data class AmountChange(
        val amount: BigDecimal,
    ) : SendFeatureAction

    data class RestoreSelectedToken(val token: Token.Active) : SendFeatureAction

    data class SetupInitialToken(
        val initialToken: Token.Active? = null
    ) : SendFeatureAction
}
