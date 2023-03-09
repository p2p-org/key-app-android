package org.p2p.wallet.swap.jupiter.interactor

import timber.log.Timber
import java.math.BigDecimal
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.utils.toBase58Instance

private const val LOW_SLIPPAGE_ERROR_CODE = "SlippageToleranceExceeded"
private const val LOW_SLIPPAGE_ERROR_MESSAGE = "Slippage tolerance exceeded"

class JupiterSwapInteractor(
    private val relaySdkFacade: RelaySdkFacade,
    private val tokenKeyProvider: TokenKeyProvider,
    private val swapTransactionRepository: JupiterSwapTransactionRepository,
    private val rpcSolanaRepository: RpcSolanaRepository
) {
    class LowSlippageRpcError(cause: ServerException) : Throwable(cause.message)

    sealed interface JupiterSwapTokensResult {
        object Success : JupiterSwapTokensResult
        data class Failure(override val cause: Throwable) : Throwable(), JupiterSwapTokensResult
    }

    suspend fun swapTokens(route: JupiterSwapRoute): JupiterSwapTokensResult = try {
        val swapTransaction = swapTransactionRepository.createSwapTransactionForRoute(
            route = route,
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        )
        val userAccount = Account(tokenKeyProvider.keyPair)

        val signedSwapTransaction = relaySdkFacade.signTransaction(
            transaction = swapTransaction,
            keyPair = userAccount.getEncodedKeyPair().toBase58Instance(),
            // empty string because swap transaction already has recent blockhash
            // if pass our own recent blockhash, there is an error
            recentBlockhash = null
        )
        val firstTransactionSignature = rpcSolanaRepository.sendTransaction(
            serializedTransaction = signedSwapTransaction.transaction.base58Value,
            encoding = Encoding.BASE58
        )
        Timber.i("Swap tokens success: $firstTransactionSignature")
        JupiterSwapTokensResult.Success
    } catch (error: ServerException) {
        val errorMessage = error.message.orEmpty()
        if (LOW_SLIPPAGE_ERROR_CODE in errorMessage ||
            LOW_SLIPPAGE_ERROR_MESSAGE in errorMessage
        ) {
            JupiterSwapTokensResult.Failure(LowSlippageRpcError(error))
        } else {
            JupiterSwapTokensResult.Failure(error)
        }
    } catch (failure: Throwable) {
        JupiterSwapTokensResult.Failure(failure)
    }

    fun getSwapTokenPair(state: SwapState): Pair<SwapTokenModel?, SwapTokenModel?> {
        return state.run {
            when (this) {
                SwapState.InitialLoading -> null to null
                is SwapState.LoadingRoutes -> tokenA to tokenB
                is SwapState.LoadingTransaction -> tokenA to tokenB
                is SwapState.SwapException -> getSwapTokenPair(previousFeatureState)
                is SwapState.SwapLoaded -> tokenA to tokenB
                is SwapState.TokenAZero -> tokenA to tokenB
                is SwapState.TokenANotZero -> tokenA to tokenB
            }
        }
    }

    fun getTokenAAmount(state: SwapState): BigDecimal? {
        val tokenA = when (state) {
            is SwapState.LoadingRoutes -> state.tokenA
            is SwapState.LoadingTransaction -> state.tokenA
            is SwapState.SwapLoaded -> state.tokenA
            is SwapState.TokenAZero -> state.tokenA
            is SwapState.TokenANotZero -> state.tokenA
            SwapState.InitialLoading,
            is SwapState.SwapException.FeatureExceptionWrapper,
            is SwapState.SwapException.OtherException -> null
        }
        return (tokenA as? SwapTokenModel.UserToken)?.details?.total
    }

    fun getPriceImpact(state: SwapState?): BigDecimal? {
        return when (state) {
            null,
            SwapState.InitialLoading,
            is SwapState.LoadingRoutes,
            is SwapState.TokenANotZero,
            is SwapState.TokenAZero -> null
            is SwapState.SwapException -> getPriceImpact(state.previousFeatureState)

            is SwapState.LoadingTransaction -> state.routes.getOrNull(state.activeRoute)?.priceImpactPct
            is SwapState.SwapLoaded -> state.routes.getOrNull(state.activeRoute)?.priceImpactPct
        }
    }
}
