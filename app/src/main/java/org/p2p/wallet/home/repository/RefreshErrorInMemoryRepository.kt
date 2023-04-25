package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RefreshErrorInMemoryRepository : RefreshErrorRepository {

    private val flow = MutableSharedFlow<Unit>(replay = 1)

    override fun getRefreshClickFlow(): Flow<Unit> = flow.asSharedFlow()
    override fun notifyRefreshClicked() {
        flow.tryEmit(Unit)
    }
}
