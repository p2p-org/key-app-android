package org.p2p.wallet.history.model

sealed interface HistoryPagingResult {

    data class Success(val data: List<HistoryTransaction>) : HistoryPagingResult

    data class Error(override val cause: Throwable) : HistoryPagingResult, Throwable()
}
