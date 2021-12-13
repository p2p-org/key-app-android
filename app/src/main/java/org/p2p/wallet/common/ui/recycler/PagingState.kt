package org.p2p.wallet.common.ui.recycler

sealed class PagingState {
    object Idle : PagingState()
    object Loading : PagingState()
    object InitialLoading : PagingState()
    class Error(val e: Throwable) : PagingState()
}