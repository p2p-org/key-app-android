package org.p2p.wallet.common.ui.recycler

/**
 * Common state class for adapters that need paging
 */
sealed interface PagingState {
    object Idle : PagingState
    object Loading : PagingState
    object InitialLoading : PagingState
    data class Error(val error: Throwable) : PagingState
}
