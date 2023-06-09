package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token
import org.p2p.wallet.newsend.model.main.WidgetState
import org.p2p.wallet.transaction.model.NewShowProgress

sealed interface SendCommonState {
    object Idle : SendCommonState

    data class WidgetUpdate(val widgetState: WidgetState) : SendCommonState

    data class Loading(val loadingState: FeeLoadingState) : SendCommonState

    data class ShowProgress(
        val internalUUID: String,
        val data: NewShowProgress
    ) : SendCommonState

    data class ShowTokenSelection(
        val currentToken: Token.Active,
        private val timestamp: Long = System.nanoTime()
    ) : SendCommonState

    data class ShowTransactionDetails(
        val feeTotal: SendFeeTotal,
        private val timestamp: Long = System.nanoTime()
    ) : SendCommonState

    data class GeneralError(val cause: Throwable) : SendCommonState

    data class ShowFreeTransactionDetails(
        private val timestamp: Long = System.nanoTime()
    ) : SendCommonState
}
