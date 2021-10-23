package org.p2p.wallet.swap.repository

import org.p2p.wallet.main.model.Token
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaSwapRequest
import org.p2p.wallet.swap.model.orca.PoolConverter
import org.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey

class OrcaSwapRemoteRepository(
    private val rpcRepository: RpcRepository
) : OrcaSwapRepository {

    override suspend fun loadPools(swapProgramId: String): List<OrcaPool> {
        val publicKey = PublicKey(swapProgramId)
        return rpcRepository.getPools(publicKey).map { PoolConverter.fromNetwork(it) }
    }

    override suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance {
        val response = rpcRepository.getTokenAccountBalance(publicKey)
        return AccountBalance(publicKey, response.amount)
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
}