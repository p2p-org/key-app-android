package org.p2p.wallet.history.model

sealed class HistoryPagingResult {

    data class Success(val data: List<HistoryTransaction>) : HistoryPagingResult()

    data class Error(val cause: Throwable) : HistoryPagingResult()
}
