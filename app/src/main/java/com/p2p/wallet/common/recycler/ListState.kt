package com.p2p.wallet.common.recycler

sealed class ListState {
    object Initial : ListState()
    object Idle : ListState()
    object Loading : ListState()
    object Refreshing : ListState()
    class Error(val e: Throwable) : ListState()
}