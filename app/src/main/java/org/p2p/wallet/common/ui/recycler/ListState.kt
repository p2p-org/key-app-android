package org.p2p.wallet.common.ui.recycler

sealed class ListState {
    object Initial : ListState()
    object Idle : ListState()
    object Loading : ListState()
    object Refreshing : ListState()
    class Error(val e: Throwable) : ListState()
}
