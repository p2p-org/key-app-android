package org.p2p.wallet.newsend.smartselection

import java.math.BigDecimal
import org.p2p.core.token.Token

sealed interface UserSendAction {

    data class SimpleInitialization(
        val defaultToken: Token.Active,
        val recipient: String
        ) : UserSendAction

    data class AmountInitialization(
        val defaultToken: Token.Active,
        val recipient: String,
        val initialAmount: BigDecimal
    ) : UserSendAction

    data class AmountChanged(val inputAmount: BigDecimal) : UserSendAction

    data class SourceTokenChanged(val newToken: Token.Active) : UserSendAction

    data class FeePayerChanged(val newFeePayer: Token.Active) : UserSendAction

    data class MaxInputEntered(val inputAmount: BigDecimal) : UserSendAction

    object ToggleInputMode : UserSendAction
}
