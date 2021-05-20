package com.p2p.wallet.swap.repository

import com.p2p.wallet.common.network.Constants
import com.p2p.wallet.swap.model.SwapRequest
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.TokenSwap
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.types.TokenAccountBalance

class SwapRemoteRepository(
    private val client: RpcClient
) : SwapRepository {

    override suspend fun loadPoolInfoList(swapProgramId: String): List<Pool.PoolInfo> = withContext(Dispatchers.IO) {
        val publicKey = PublicKey(swapProgramId)
        return@withContext Pool.getPools(client, publicKey)
    }

    override suspend fun loadTokenBalance(publicKey: PublicKey): TokenAccountBalance = withContext(Dispatchers.IO) {
        TokenTransaction.getTokenAccountBalance(client, publicKey)
    }

    override suspend fun swap(
        keys: List<String>,
        request: SwapRequest,
        accountA: Token?,
        accountB: Token?
    ): String = withContext(Dispatchers.IO) {
        val owner = Account.fromMnemonic(keys, "")

        val tokenSwap = TokenSwap(client)

        return@withContext tokenSwap.swap(
            owner,
            request.pool,
            request.slippage,
            request.amountIn,
            request.balanceA,
            request.balanceB,
            PublicKey(Constants.WRAPPED_SOL_MINT),
            accountA?.publicKey?.toPublicKey(),
            accountB?.publicKey?.toPublicKey()
        )
    }
}