package org.p2p.wallet.home.interactor

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RefreshErrorInteractor {

    private val refreshClickFlow = MutableSharedFlow<Unit>(replay = 1)

    fun getRefreshClickFlow() = refreshClickFlow.asSharedFlow()

    fun notifyRefreshClick() {
        refreshClickFlow.tryEmit(Unit)
    }
}
