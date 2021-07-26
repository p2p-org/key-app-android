package com.p2p.wallet.swap.repository

import com.p2p.wallet.common.network.Constants
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.model.SwapRequest
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.TokenSwap
import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.types.TokenAccountBalance

class SwapRemoteRepository(
    private val rpcRepository: RpcRepository
) : SwapRepository {

    override suspend fun loadPoolInfoList(swapProgramId: String): List<Pool.PoolInfo> {
        val publicKey = PublicKey(swapProgramId)
        return rpcRepository.getPools(publicKey)
    }

    override suspend fun loadTokenBalance(publicKey: PublicKey): TokenAccountBalance =
        rpcRepository.getTokenAccountBalance(publicKey)

    override suspend fun swap(
        keys: List<String>,
        request: SwapRequest,
        accountA: Token?,
        accountB: Token?
    ): String = withContext(Dispatchers.IO) {
        val owner = Account.fromMnemonic(keys, "")

        val tokenSwap = TokenSwap()

        return@withContext tokenSwap.swap(
            owner = owner,
            pool = request.pool,
            slippage = request.slippage,
            amountIn = request.amount,
            balanceA = request.balanceA,
            balanceB = request.balanceB,
            wrappedSolAccount = Constants.WRAPPED_SOL_MINT.toPublicKey(),
            accountAddressA = accountA?.publicKey?.toPublicKey(),
            accountAddressB = accountB?.publicKey?.toPublicKey(),
            getAccountInfo = { rpcRepository.getAccountInfo(it) },
            getBalanceNeeded = { rpcRepository.getMinimumBalanceForRentExemption(it) },
            sendTransaction = { transaction, signers ->
                val recentBlockhash = rpcRepository.getRecentBlockhash()
                transaction.setRecentBlockHash(recentBlockhash.recentBlockhash)
                transaction.sign(signers)
                rpcRepository.sendTransaction(transaction)
            }
        )
    }
}