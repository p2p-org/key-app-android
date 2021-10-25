package org.p2p.wallet.swap.repository

import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.model.orca.OrcaSwapRequest
import org.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.ws.NotificationEventListener
import org.p2p.solanaj.ws.SubscriptionWebSocketClient

class OrcaSwapRemoteRepository(
    private val rpcRepository: RpcRepository,
    private val environmentManager: EnvironmentManager
) : OrcaSwapRepository {

    override suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance {
        val response = rpcRepository.getTokenAccountBalance(publicKey)
        return AccountBalance(publicKey, response.amount, response.value.decimals)
    }

    override suspend fun swap(
        account: Account,
        request: OrcaSwapRequest,
        accountA: Token.Active?,
        associatedAddress: PublicKey,
        shouldCreateAssociatedInstruction: Boolean
    ): String = withContext(Dispatchers.IO) {
        val tokenSwap = TokenSwap()

        return@withContext tokenSwap.swap(
            owner = account,
            pool = request.pool,
            slippage = request.slippage,
            amountIn = request.amount,
            balanceA = request.balanceA,
            balanceB = request.balanceB,
            sourceMint = request.sourceMint,
            destinationMint = request.destinationMint,
            wrappedSolAccount = Token.WRAPPED_SOL_MINT.toPublicKey(),
            accountAddressA = accountA?.publicKey?.toPublicKey(),
            associatedAddress = associatedAddress,
            shouldCreateAssociatedInstruction = shouldCreateAssociatedInstruction,
            getAccountInfo = { rpcRepository.getAccountInfo(it) },
            getBalanceNeeded = { rpcRepository.getMinimumBalanceForRentExemption(it) },
            getRecentBlockhash = { rpcRepository.getRecentBlockhash().recentBlockhash },
            sendTransaction = { transaction ->
                rpcRepository.sendTransaction(transaction)
            }
        )
    }

    override suspend fun sendAndWait(serializedTransaction: String, onConfirmed: () -> Unit) {
        val signature = rpcRepository.sendTransaction(serializedTransaction)

        val listener = NotificationEventListener { onConfirmed() }

        val endpoint = environmentManager.loadEnvironment().endpoint
        val wssClient = SubscriptionWebSocketClient.getInstance(endpoint)
        wssClient.signatureSubscribe(signature, listener)
    }
}