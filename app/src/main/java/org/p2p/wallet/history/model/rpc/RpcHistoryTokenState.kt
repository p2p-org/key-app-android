package org.p2p.wallet.history.model.rpc

import org.p2p.wallet.history.model.HistoryPagingState

data class RpcHistoryTokenState(
    val address: String? = null,
    val pagingState: HistoryPagingState = HistoryPagingState.ACTIVE
)
