package org.p2p.wallet.auth.gateway.repository.mapper

sealed interface GatewayResult {
    open class Success<T>(val data: T) : GatewayResult
}
