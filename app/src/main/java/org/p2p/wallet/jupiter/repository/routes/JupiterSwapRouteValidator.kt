package org.p2p.wallet.jupiter.repository.routes

import timber.log.Timber
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.rpc.TransactionSimulationResult
import org.p2p.wallet.common.feature_toggles.toggles.remote.SwapRoutesValidationEnabledFeatureToggle
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance

private const val TAG = "JupiterSwapRouteValidator"

class JupiterSwapRouteValidator(
    private val dispatchers: CoroutineDispatchers,
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val swapTransactionRepository: JupiterSwapTransactionRepository,
    private val relaySdkFacade: RelaySdkFacade,
    private val tokenKeyProvider: TokenKeyProvider,
    private val swapRoutesValidationEnabled: SwapRoutesValidationEnabledFeatureToggle
) {
    private class SwapRouteValidation(
        val route: JupiterSwapRoute,
        val ordinal: Int,
        val isRouteValid: Boolean,
        val error: String?
    )

    private val userPublicKey: Base58String
        get() = tokenKeyProvider.publicKey.toBase58Instance()

    suspend fun validateRoutes(
        routes: List<JupiterSwapRoute>,
    ): List<JupiterSwapRoute> = withContext(dispatchers.io) {
        if (!swapRoutesValidationEnabled.isFeatureEnabled) {
            return@withContext routes
        }

        Timber.tag(TAG).i("Validating routes: ${routes.size}")
        val validatingRoutesJobs = routes.mapIndexed { index, route ->
            async { validateRoute(route, index) }
        }
        validatingRoutesJobs.awaitAll()
            .onEach {
                Timber.i(
                    buildString {
                        append("route number: ${it.ordinal} ")
                        append("isValid=${it.isRouteValid} ")
                        append("error=${it.error} ")
                    }
                )
            }
            .asSequence()
            .filter(SwapRouteValidation::isRouteValid)
            .sortedByDescending(SwapRouteValidation::ordinal) // to save order
            .map(SwapRouteValidation::route)
            .toList()
            .also {
                Timber.tag(TAG).i("Validating routes finished, total valid routes = ${it.size}; was = ${routes.size}")
            }
    }

    private suspend fun validateRoute(
        route: JupiterSwapRoute,
        ordinal: Int
    ): SwapRouteValidation {
        Timber.tag(TAG).i("Validating routes started for: $ordinal")
        val isRouteValid = checkThatRouteValid(route)
        return SwapRouteValidation(
            route = route,
            ordinal = ordinal,
            isRouteValid = isRouteValid.isSimulationSuccess,
            error = isRouteValid.errorIfSimulationFailed
        )
    }

    private suspend fun checkThatRouteValid(route: JupiterSwapRoute): TransactionSimulationResult {
        return try {
            val userAccount = Account(tokenKeyProvider.keyPair)
            val routeTransaction = swapTransactionRepository.createSwapTransactionForRoute(route, userPublicKey)
            val signedSwapTransaction = relaySdkFacade.signTransaction(
                transaction = routeTransaction,
                keyPair = userAccount.getEncodedKeyPair().toBase58Instance(),
                // empty string because swap transaction already has recent blockhash
                // if pass our own recent blockhash, there is an error
                recentBlockhash = null
            )
            rpcSolanaRepository.simulateTransaction(
                serializedTransaction = signedSwapTransaction.transaction.base58Value,
                encoding = Encoding.BASE58
            )
        } catch (error: Throwable) {
            Timber.i(error, "Something went wrong while validating route")
            TransactionSimulationResult(false, null)
        }
    }
}
