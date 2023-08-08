package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token
import org.p2p.wallet.transaction.model.NewShowProgress

sealed interface SendState {
    object Idle : SendState

    data class WidgetUpdate(val widgetState: WidgetState) : SendState

    data class CalculationUpdate(val calculationEvent: CalculationEvent) : SendState

    data class FeePayerUpdate(val feePayerUpdate: FeePayerState) : SendState

    data class Loading(val loadingState: FeeLoadingState) : SendState

    data class ShowProgress(
        val internalTransactionUuid: String,
        val data: NewShowProgress
    ) : SendState

    data class ShowTokenSelection(val currentToken: Token.Active) : SendState

    data class ShowTransactionDetails(val feeTotal: SendFeeTotal) : SendState

    data class GeneralError(val cause: Throwable) : SendState

    object ShowFreeTransactionDetails : SendState
}
