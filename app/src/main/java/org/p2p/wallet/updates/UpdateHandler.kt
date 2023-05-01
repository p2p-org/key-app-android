package org.p2p.wallet.updates

import com.google.gson.JsonObject

interface UpdateHandler {
    suspend fun initialize()
    suspend fun onUpdate(type: UpdateType, data: JsonObject)
}
