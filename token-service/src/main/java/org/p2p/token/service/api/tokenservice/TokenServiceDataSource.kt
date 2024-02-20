package org.p2p.token.service.api.tokenservice

import org.p2p.core.rpc.JsonRpc
import org.p2p.token.service.model.TokenServiceResult

internal interface TokenServiceDataSource {
    suspend fun <P, T> launch(request: JsonRpc<P, T>): TokenServiceResult<T>
}
