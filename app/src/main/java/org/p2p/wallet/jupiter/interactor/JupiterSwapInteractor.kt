package org.p2p.wallet.jupiter.interactor

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.utils.isLessThan
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.infrastructure.network.data.InstructionErrorType
import org.p2p.wallet.infrastructure.network.data.RpcError
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.interactor.model.SwapPriceImpactType
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.utils.toBase58Instance

private const val LOW_SLIPPAGE_ERROR_CODE = 6001

private const val TAG = "JupiterSwapInteractor"

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
        Timber.tag(TAG).i("Starting swapping tokens: jupiter unsigned transaction = ${jupiterTransaction.base64Value}")
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
        Timber.tag(TAG).i(error, "Failed swapping transaction")
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
        Timber.tag(TAG).i(failure, "Failed to swap")
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

    fun getPriceImpact(state: SwapState?): SwapPriceImpactType {
        state ?: return SwapPriceImpactType.None
        val activeRoute: JupiterSwapRoute = state.getActiveRoute() ?: return SwapPriceImpactType.None
        val currentSlippage: Slippage = state.getCurrentSlippage() ?: return SwapPriceImpactType.None

        val priceImpactPercent: BigDecimal = activeRoute.priceImpactPct
        val totalFeeAndDeposits: BigInteger = activeRoute.fees.totalFeeAndDepositsInSol
        val outAmount: BigInteger = activeRoute.outAmountInLamports
        val slippageValue: Int = activeRoute.slippageBps

        val threePercent = BigDecimal.valueOf(0.03)
        val onePercent = BigDecimal.valueOf(0.01)

        return when {
            priceImpactPercent.isLessThan(onePercent) -> {
                SwapPriceImpactType.None
                // commented due to PWN-8092 new requirements
                // this codes goes to 2.7.0, but needed in 2.7.1 so i removed it temporarily
//                val isHighFeesNotFound = checkForHighFees(totalFeeAndDeposits, outAmount, slippageValue)
//                if (isHighFeesNotFound) {
//                    SwapPriceImpactType.None
//                } else {
//                    SwapPriceImpactType.HighFees(currentSlippage)
//                }
            }
            priceImpactPercent.isLessThan(threePercent) -> {
                SwapPriceImpactType.HighPriceImpact(priceImpactPercent, SwapPriceImpactType.HighPriceImpactType.YELLOW)
            }
            else -> {
                SwapPriceImpactType.HighPriceImpact(priceImpactPercent, SwapPriceImpactType.HighPriceImpactType.RED)
            }
        }
    }

    private fun SwapState.getActiveRoute(): JupiterSwapRoute? {
        return when (this) {
            SwapState.InitialLoading,
            is SwapState.LoadingRoutes,
            is SwapState.TokenANotZero,
            is SwapState.TokenAZero -> null

            is SwapState.SwapException -> previousFeatureState.getActiveRoute()

            is SwapState.LoadingTransaction -> routes.getOrNull(activeRoute)
            is SwapState.RoutesLoaded -> routes.getOrNull(activeRoute)
            is SwapState.SwapLoaded -> routes.getOrNull(activeRoute)
        }
    }

    private fun SwapState.getCurrentSlippage(): Slippage? {
        return when (this) {
            SwapState.InitialLoading -> SwapStateManager.DEFAULT_SLIPPAGE
            is SwapState.LoadingRoutes -> slippage
            is SwapState.LoadingTransaction -> slippage
            is SwapState.SwapLoaded -> slippage
            is SwapState.TokenAZero -> slippage
            is SwapState.TokenANotZero -> slippage
            is SwapState.RoutesLoaded -> slippage
            is SwapState.SwapException -> null
        }
    }

    /**
     * We need to cast BigIntegers to Double to keep numbers after dot when dividing.
     * We had an error when we divided 2039280 / 955187 and got only 2 instead of full 2.13
     * which caused invalid behaviour in case of slippage 2.1%
     */
    private fun checkForHighFees(
        totalFeesAndDeposit: BigInteger,
        outAmount: BigInteger,
        slippageValuePercent: Int
    ): Boolean {
        val feesValuePercent: Int =
            (totalFeesAndDeposit.toDouble() / outAmount.toDouble())
                .times(100)
                .toInt()
        return feesValuePercent <= slippageValuePercent
    }
}
