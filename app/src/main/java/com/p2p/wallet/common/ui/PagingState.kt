package com.p2p.wallet.common.ui

sealed class PagingState {
    object Idle : PagingState()
    object Loading : PagingState()
    object Refreshing : PagingState()
    class Error(val e: Throwable) : PagingState()
}