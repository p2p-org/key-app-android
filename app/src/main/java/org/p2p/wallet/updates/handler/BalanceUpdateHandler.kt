package org.p2p.wallet.updates.handler

import timber.log.Timber
import org.p2p.wallet.updates.UpdateHandler
import org.p2p.wallet.updates.UpdateType

class BalanceUpdateHandler() : UpdateHandler {

    override suspend fun initialize() {}

    override suspend fun onUpdate(type: UpdateType, data: Any) {
        if (type != UpdateType.BALANCE_RECEIVED) {
            return
        }
        Timber.tag(TAG).d("Balance received, data = $data")
    }

    companion object {
        private const val TAG = "BalanceUpdateManager"
    }
}
