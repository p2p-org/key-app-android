package org.p2p.wallet.jupiter.interactor

import timber.log.Timber
import java.math.BigDecimal
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.infrastructure.network.data.InstructionErrorType
import org.p2p.wallet.infrastructure.network.data.RpcError
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.utils.toBase58Instance

private const val LOW_SLIPPAGE_ERROR_CODE = 6001

class JupiterSwapInteractor(
    private val relaySdkFacade: RelaySdkFacade,
    private val tokenKeyProvider: TokenKeyProvider,
    private val rpcSolanaRepository: RpcSolanaRepository
) {
    class LowSlippageRpcError(cause: ServerException) : Throwable(cause.message)

    sealed interface JupiterSwapTokensResult {
        data class Success(val signature: String) : JupiterSwapTokensResult
        data class Failure(override val cause: Throwable) : Throwable(), JupiterSwapTokensResult
    }

    suspend fun swapTokens(jupiterTransaction: Base64String): JupiterSwapTokensResult = try {
        val userAccount = Account(tokenKeyProvider.keyPair)

        val signedSwapTransaction = relaySdkFacade.signTransaction(
            transaction = jupiterTransaction,
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
        JupiterSwapTokensResult.Success(firstTransactionSignature)
    } catch (error: ServerException) {
        val domainErrorType = error.domainErrorType
        if (domainErrorType != null &&
            domainErrorType is RpcError.InstructionError &&
            domainErrorType.instructionErrorType is InstructionErrorType.Custom &&
            domainErrorType.instructionErrorType.programErrorId == LOW_SLIPPAGE_ERROR_CODE.toLong()
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
                is SwapState.RoutesLoaded -> tokenA to tokenB
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
            is SwapState.RoutesLoaded -> state.tokenA
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
            is SwapState.RoutesLoaded -> state.routes.getOrNull(state.activeRoute)?.priceImpactPct
            is SwapState.SwapLoaded -> state.routes.getOrNull(state.activeRoute)?.priceImpactPct
        }
    }
}
