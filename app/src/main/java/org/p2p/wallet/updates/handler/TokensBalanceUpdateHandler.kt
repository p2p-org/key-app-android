package org.p2p.wallet.updates.handler

import com.google.gson.Gson
import com.google.gson.JsonObject
import timber.log.Timber
import org.p2p.wallet.updates.UpdateHandler
import org.p2p.wallet.updates.UpdateType

class TokensBalanceUpdateHandler(
    private val gson: Gson
) : UpdateHandler {

    override suspend fun initialize() {}

    override suspend fun onUpdate(type: UpdateType, data: JsonObject) {
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
