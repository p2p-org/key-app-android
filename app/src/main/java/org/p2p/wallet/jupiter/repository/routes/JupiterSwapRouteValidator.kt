package org.p2p.wallet.jupiter.repository.routes

import timber.log.Timber
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.data.ServerException
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.rpc.TransactionSimulationResult
import org.p2p.wallet.common.feature_toggles.toggles.remote.SwapRoutesValidationEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.interactor.JupiterSwapTokensResult
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.sdk.facade.RelaySdkFacade

private const val TAG = "JupiterSwapRouteValidator"

private const val JUPITER_LOW_SLIPPAGE_ERROR_CODE = 6001L
private const val WHIRPOOLS_INVALID_TIMESTAMP_ERROR_CODE = 6022L

class JupiterSwapRouteValidator(
    private val dispatchers: CoroutineDispatchers,
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val rpcBlockhashRepository: RpcBlockhashRepository,
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

    suspend fun validateRouteV6(
        route: JupiterSwapRouteV6,
    ): JupiterSwapRouteV6? {
        if (!swapRoutesValidationEnabled.isFeatureEnabled) {
            return route
        }

        Timber.tag(TAG).i("Validating routes")
        return if (simulateRouteV6(route).isSimulationSuccess) route else null
    }

    private suspend fun simulateRouteV6(route: JupiterSwapRouteV6): TransactionSimulationResult {
        return try {
            val userAccount = Account(tokenKeyProvider.keyPair)
            val routeTransaction = swapTransactionRepository.createSwapTransactionForRoute(route, userPublicKey)
            val latestBlockhash = rpcBlockhashRepository.getRecentBlockhash()
            val signedSwapTransaction = relaySdkFacade.signTransaction(
                transaction = routeTransaction,
                keyPair = userAccount.getEncodedKeyPair().toBase58Instance(),
                // empty string because swap transaction already has recent blockhash
                // if pass our own recent blockhash, there is an error
                recentBlockhash = latestBlockhash
            ).transaction.convertToBase64()
            rpcSolanaRepository.simulateTransaction(
                serializedTransaction = signedSwapTransaction.base64Value,
                encoding = Encoding.BASE64
            )
        } catch (blockchainError: ServerException) {
            val swapFailure = handleBlockchainError(blockchainError)

            when (swapFailure.cause) {
                is JupiterSwapTokensResult.Failure.InvalidTimestampRpcError -> {
                    TransactionSimulationResult(
                        isSimulationSuccess = true,
                        errorIfSimulationFailed = null
                    )
                }
                else -> {
                    TransactionSimulationResult(
                        isSimulationSuccess = false,
                        errorIfSimulationFailed = null
                    )
                }
            }
        } catch (error: Throwable) {
            Timber.i(error, "Something went wrong while validating route")
            TransactionSimulationResult(
                isSimulationSuccess = false,
                errorIfSimulationFailed = error.message
            )
        }
    }

    private fun handleBlockchainError(
        blockchainError: ServerException
    ): JupiterSwapTokensResult.Failure {
        val domainErrorType = blockchainError.domainErrorType
        return when {
            domainErrorType !is RpcTransactionError.InstructionError -> {
                blockchainError
            }
            domainErrorType.isLowSlippageError() -> {
                JupiterSwapTokensResult.Failure.LowSlippageRpcError(blockchainError)
            }
            domainErrorType.isInvalidTimestampError() -> {
                JupiterSwapTokensResult.Failure.InvalidTimestampRpcError(blockchainError)
            }
            else -> {
                blockchainError
            }
        }.let(JupiterSwapTokensResult::Failure)
    }

    private fun RpcTransactionError.InstructionError.isLowSlippageError(): Boolean =
        extractCustomErrorCodeOrNull() == JUPITER_LOW_SLIPPAGE_ERROR_CODE

    /**
     * @see [org.p2p.wallet.jupiter.interactor.JupiterSwapTokensResult.Failure.InvalidTimestampRpcError]
     */
    private fun RpcTransactionError.InstructionError.isInvalidTimestampError(): Boolean =
        extractCustomErrorCodeOrNull() == WHIRPOOLS_INVALID_TIMESTAMP_ERROR_CODE
}
