package org.p2p.wallet.updates

interface UpdateHandler {
    suspend fun initialize()
    suspend fun onUpdate(type: UpdateType, data: Any)
}
