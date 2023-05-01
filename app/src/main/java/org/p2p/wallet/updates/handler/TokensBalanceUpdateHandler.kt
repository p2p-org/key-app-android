package org.p2p.wallet.updates.handler

import timber.log.Timber
import org.p2p.wallet.updates.UpdateHandler
import org.p2p.wallet.updates.UpdateType

class TokensBalanceUpdateHandler() : UpdateHandler {

    override suspend fun initialize() {}

    override suspend fun onUpdate(type: UpdateType, data: Any) {
        if (type != UpdateType.TOKEN_BALANCES_RECEIVED) {
            return
        }
        Timber.tag(TAG).d("Tokens balance received, data = $data")
    }

    companion object {
        const val PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
        private const val TAG = "TokensBalanceUpdateManager"
    }
}
