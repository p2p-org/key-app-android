package org.p2p.wallet.common.ui.recycler

sealed interface PagingState {
    object Idle : PagingState
    object Loading : PagingState
    object InitialLoading : PagingState
    data class Error(val error: Throwable) : PagingState
}
