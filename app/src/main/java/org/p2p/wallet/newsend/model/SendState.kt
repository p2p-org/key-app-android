package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token
import org.p2p.wallet.newsend.model.main.WidgetState

sealed interface SendState {
    object Idle : SendState

    data class CalculationUpdate(val calculationState: CalculationState) : SendState

    data class FeePayerUpdate(val feePayerState: FeePayerState) : SendState

    data class WidgetUpdate(val widgetState: WidgetState) : SendState

    data class Loading(val loadingState: FeeLoadingState) : SendState

    object ShowFreeTransactionDetails : SendState

    data class ShowTokenSelection(val currentToken: Token.Active) : SendState

    data class ShowTransactionDetails(val feeTotal: SendFeeTotal) : SendState

    data class GeneralError(val cause: Throwable) : SendState

    fun isInitialized(): Boolean = this !is Idle
}
