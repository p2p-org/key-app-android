package org.p2p.wallet.swap.jupiter.domain

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

class JupiterSwapInteractor(
    private val relaySdkFacade: RelaySdkFacade,
    private val tokenKeyProvider: TokenKeyProvider,
    private val swapTransactionRepository: JupiterSwapTransactionRepository,
    private val rpcBlockhashRepository: RpcBlockhashRepository,
    private val rpcSolanaRepository: RpcSolanaRepository
) {
    sealed interface JupiterSwapTokensResult {
        object Success : JupiterSwapTokensResult
        data class Failure(override val cause: Throwable) : Throwable(), JupiterSwapTokensResult
    }

    suspend fun swapTokens(route: JupiterSwapRoute): JupiterSwapTokensResult = try {
        val swapTransaction = swapTransactionRepository.createSwapTransactionForRoute(
            route = route,
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        )
        val latestBlockhash = rpcBlockhashRepository.getRecentBlockhash()
        val userAccount = Account(tokenKeyProvider.keyPair)

        val signedSwapTransaction = relaySdkFacade.signTransaction(
            transaction = swapTransaction,
            keyPair = userAccount.getEncodedKeyPair().toBase58Instance(),
            recentBlockhash = latestBlockhash
        )
        val firstTransactionSignature = rpcSolanaRepository.sendTransaction(
            serializedTransaction = signedSwapTransaction.transaction.base58Value,
            encoding = Encoding.BASE58
        )
        Timber.i("Swap tokens success: $firstTransactionSignature")
        JupiterSwapTokensResult.Success
    } catch (failure: Throwable) {
        JupiterSwapTokensResult.Failure(failure)
    }
}
