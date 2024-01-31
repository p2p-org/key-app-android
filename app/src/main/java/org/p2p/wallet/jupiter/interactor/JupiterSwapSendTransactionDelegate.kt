package org.p2p.wallet.jupiter.interactor

import timber.log.Timber
import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.network.data.ServerException
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapTransactionRpcErrorMapper
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.sdk.facade.RelaySdkFacade

private const val TAG = "JupiterSendSwapTransactionDelegate"

class JupiterSwapSendTransactionDelegate(
    private val rpcSolanaRepository: RpcSolanaRepository,
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val swapTransactionRepository: JupiterSwapTransactionRepository,
    private val rpcErrorMapper: JupiterSwapTransactionRpcErrorMapper,
    private val tokenKeyProvider: TokenKeyProvider,
    private val relaySdkFacade: RelaySdkFacade
) {
    private var retryCount: Int = 0

    suspend fun sendSwapTransaction(
        swapRoute: JupiterSwapRouteV6,
        jupiterTransaction: Base64String
    ): JupiterSwapTokensResult = try {
        retryCount++
        Timber.tag(TAG).i("Starting swapping tokens: jupiter unsigned transaction = ${jupiterTransaction.base64Value}")
        val userAccountKeypair = Account(tokenKeyProvider.keyPair)
            .getEncodedKeyPair()
            .toBase58Instance()

        val signedSwapTransaction = relaySdkFacade.signTransaction(
            transaction = jupiterTransaction,
            keyPair = userAccountKeypair,
            recentBlockhash = null // jupiterTransaction already contains blockhash
        ).transaction.convertToBase64()

        val firstTransactionSignature = rpcSolanaRepository.sendTransaction(
            serializedTransaction = signedSwapTransaction.base64Value,
            encoding = Encoding.BASE64
        )
        Timber.i("Swap tokens success: $firstTransactionSignature")

        JupiterSwapTokensResult.Success(firstTransactionSignature)
    } catch (blockchainError: ServerException) {
        val swapFailure = rpcErrorMapper.mapRpcError(blockchainError)

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

    private suspend fun generateNewSwapTransaction(route: JupiterSwapRouteV6): Base64String? = try {
        swapTransactionRepository.createSwapTransactionForRoute(route, tokenKeyProvider.publicKeyBase58)
    } catch (generationFailed: Throwable) {
        Timber.tag(TAG).i(generationFailed)
        null
    }
}
