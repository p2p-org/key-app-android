package org.p2p.wallet.jupiter.interactor

import timber.log.Timber
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.data.transactionerrors.RpcTransactionError
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.utils.toBase58Instance

private const val TAG = "JupiterSendSwapTransactionDelegate"

private const val JUPITER_LOW_SLIPPAGE_ERROR_CODE = 6001L
private const val WHIRPOOLS_INVALID_TIMESTAMP_ERROR_CODE = 6022L

class JupiterSwapSendTransactionDelegate(
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val swapTransactionRepository: JupiterSwapTransactionRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val relaySdkFacade: RelaySdkFacade
) {
    private var retryCount: Int = 0

    suspend fun sendSwapTransaction(
        swapRoute: JupiterSwapRoute,
        jupiterTransaction: Base64String
    ): JupiterSwapTokensResult = try {
        retryCount++
        Timber.tag(TAG).i("Starting swapping tokens: jupiter unsigned transaction = ${jupiterTransaction.base64Value}")
        val userAccountKeypair = Account(tokenKeyProvider.keyPair)
            .getEncodedKeyPair()
            .toBase58Instance()

        // empty string because swap transaction already has recent blockhash
        // if pass our own recent blockhash, there is an error
        val noBlockhashValue = null

        val signedSwapTransaction = relaySdkFacade.signTransaction(
            transaction = jupiterTransaction,
            keyPair = userAccountKeypair,
            recentBlockhash = noBlockhashValue
        ).transaction.convertToBase64()

        val firstTransactionSignature = rpcSolanaRepository.sendTransaction(
            serializedTransaction = signedSwapTransaction.base64Value,
            encoding = Encoding.BASE64
        )
        Timber.i("Swap tokens success: $firstTransactionSignature")

        JupiterSwapTokensResult.Success(firstTransactionSignature)
    } catch (blockchainError: ServerException) {
        val swapFailure = handleBlockchainError(blockchainError)

        when (swapFailure.cause) {
            is JupiterSwapTokensResult.Failure.InvalidTimestampRpcError -> {
                // retry again with new transaction or return original error
                generateNewSwapTransaction(swapRoute)
                    ?.takeIf { retryCount < 2 } // upper limit on retrying
                    ?.let { sendSwapTransaction(swapRoute, it) }
                    ?: swapFailure
            }
            else -> {
                swapFailure
            }
        }
    } catch (error: Throwable) {
        Timber.i(error, "Unknown error met while sending swap transaction")
        JupiterSwapTokensResult.Failure(error)
    } finally {
        retryCount = 0
    }

    private suspend fun generateNewSwapTransaction(route: JupiterSwapRoute): Base64String? = try {
        swapTransactionRepository.createSwapTransactionForRoute(route, tokenKeyProvider.publicKeyBase58)
    } catch (generationFailed: Throwable) {
        Timber.tag(TAG).i(generationFailed)
        null
    }

    private fun handleBlockchainError(
        blockchainError: ServerException
    ): JupiterSwapTokensResult.Failure {
        Timber.tag(TAG).i(blockchainError, "Failed swapping transaction")

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
