package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RefreshErrorSharedFlowRepository : RefreshErrorRepository {

    private val flow = MutableSharedFlow<Unit>(replay = 1)

    override fun getFlow() = flow.asSharedFlow()
    override fun notifyClick() {
        flow.tryEmit(Unit)
    }
}
