package org.p2p.wallet.bridge.repository

import org.p2p.core.rpc.JsonRpc
import org.p2p.wallet.bridge.model.BridgeResult

interface BridgeRepository {
    suspend fun <P, T> launch(request: JsonRpc<P, T>): BridgeResult.Success<T>
}
