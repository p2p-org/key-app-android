package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RefreshErrorInMemoryRepository : RefreshErrorRepository {

    private val flow = MutableSharedFlow<Unit>(replay = 1)

    override fun getRefreshEventFlow(): Flow<Unit> = flow.asSharedFlow()
    override fun notifyEventRefreshed() {
        flow.tryEmit(Unit)
    }
}
