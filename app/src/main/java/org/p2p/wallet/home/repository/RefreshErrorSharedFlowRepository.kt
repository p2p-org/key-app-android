package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RefreshErrorSharedFlowRepository : RefreshErrorRepository {

    private val flow = MutableSharedFlow<Unit>(replay = 1)

    override fun getRefreshClickFlow() = flow.asSharedFlow()
    override fun notifyRefreshClicked() {
        flow.tryEmit(Unit)
    }
}
