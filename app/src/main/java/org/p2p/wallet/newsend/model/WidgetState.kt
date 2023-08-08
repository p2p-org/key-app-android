package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token

sealed interface WidgetState {

    data class TokenUpdated(val updatedToken: Token.Active) : WidgetState

    data class TokenSelectionEnabled(val isEnabled: Boolean) : WidgetState

    data class DisableInput(val inputAmount: String) : WidgetState

    data class EnableCurrencySwitch(val isEnabled: Boolean) : WidgetState

    data class InputUpdated(
        val newInputValue: String,
        val isForced: Boolean = true
    ) : WidgetState
}
