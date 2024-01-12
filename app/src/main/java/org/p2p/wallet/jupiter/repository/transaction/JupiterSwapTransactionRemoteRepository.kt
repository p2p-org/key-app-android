package org.p2p.wallet.jupiter.repository.transaction

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.jupiter.api.SwapJupiterApi
import org.p2p.wallet.jupiter.api.SwapJupiterV6Api
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.SwapFailure
import org.p2p.wallet.utils.retryOnException

class JupiterSwapTransactionRemoteRepository(
    private val api: SwapJupiterApi,
    private val apiV6Api: SwapJupiterV6Api,
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
            throw SwapFailure.CreateSwapTransactionFailed(route.amountInLamports, error)
        }
    }

    override suspend fun createSwapTransactionForRoute(
        route: JupiterSwapRouteV6,
        userPublicKey: Base58String
    ): Base64String {
        return try {
            val request = mapper.toNetwork(route, userPublicKey)
            val response = retryOnException {
                apiV6Api.createRouteSwapTransaction(request)
            }
            response.versionedSwapTransaction
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            throw SwapFailure.CreateSwapTransactionFailed(route.inAmountInLamports, error)
        }
    }
}
