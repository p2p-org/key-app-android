package org.p2p.wallet.newsend.model

import java.math.BigDecimal
import org.p2p.core.token.Token

sealed interface SendActionEvent {
    object InitialLoading : SendActionEvent

    data class AmountChanged(val newInputAmount: String) : SendActionEvent

    data class SourceTokenChanged(val newSourceToken: Token.Active) : SendActionEvent

    data class ReduceAmount(val newInputAmount: BigDecimal) : SendActionEvent

    data class FeePayerManuallyChanged(val newFeePayerToken: Token.Active) : SendActionEvent

    object MaxAmountEntered : SendActionEvent

    object CurrencyModeSwitched : SendActionEvent

    object OnFeeClicked : SendActionEvent

    object OnTokenClicked : SendActionEvent

    object LaunchSending : SendActionEvent
}
