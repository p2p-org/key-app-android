package org.p2p.wallet.jupiter.repository.transaction

import kotlinx.coroutines.CancellationException
import org.p2p.core.crypto.Base64String
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.jupiter.api.SwapJupiterApi
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.SwapFailure
import org.p2p.core.crypto.Base58String
import kotlinx.coroutines.withContext
import org.p2p.wallet.utils.retryOnException

class JupiterSwapTransactionRemoteRepository(
    private val api: SwapJupiterApi,
    private val mapper: JupiterSwapTransactionMapper,
    private val dispatchers: CoroutineDispatchers
) : JupiterSwapTransactionRepository {
    override suspend fun createSwapTransactionForRoute(
        route: JupiterSwapRoute,
        userPublicKey: Base58String
    ): Base64String = withContext(dispatchers.io) {
        try {
            val request = mapper.toNetwork(route, userPublicKey)
            val response = retryOnException {
                api.createRouteSwapTransaction(request)
            }
            response.let(mapper::fromNetwork)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            throw SwapFailure.CreateSwapTransactionFailed(route, error)
        }
    }
}
