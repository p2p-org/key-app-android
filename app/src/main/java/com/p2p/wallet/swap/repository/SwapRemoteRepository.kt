package com.p2p.wallet.swap.repository

import com.p2p.wallet.common.network.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.Token
import org.p2p.solanaj.kits.TokenSwap
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.types.TokenAccountBalance
import java.math.BigDecimal

class SwapRemoteRepository(
    private val client: RpcClient
) : SwapRepository {

    override suspend fun loadPoolInfoList(swapProgramId: String): List<Pool.PoolInfo> = withContext(Dispatchers.IO) {
        val publicKey = PublicKey(swapProgramId)
        return@withContext Pool.getPools(client, publicKey)
    }

    override suspend fun loadTokenBalance(publicKey: PublicKey): TokenAccountBalance = withContext(Dispatchers.IO) {
        Token.getTokenAccountBalance(client, publicKey)
    }

    override suspend fun swap(
        keys: List<String>,
        pool: Pool.PoolInfo,
        source: String,
        destination: String,
        slippage: Double,
        amountIn: BigDecimal,
        balanceA: TokenAccountBalance,
        balanceB: TokenAccountBalance
    ): String = withContext(Dispatchers.IO) {

        val owner = Account.fromMnemonic(keys, "")

        val tokenSwap = TokenSwap(client, swapProgramId())

        return@withContext tokenSwap.swap(
            owner,
            pool,
            PublicKey(source),
            PublicKey(destination),
            slippage,
            amountIn.toBigInteger(),
            balanceA,
            balanceB,
            Constants.WRAPPED_SOL_MINT
        )
    }


    //    private fun swapProgramId(): PublicKey = when (preferenceService.getSelectedCluster()) {
//        Cluster.MAINNET -> PublicKey(Constants.MAIN_NET_PUBLIC_KEY)
//        Cluster.DEVNET -> PublicKey(Constants.DEV_NET_PUBLIC_KEY)
//        Cluster.TESTNET -> PublicKey(Constants.TEST_NET_PUBLIC_KEY)
//        else -> PublicKey("")
//    }
    private fun swapProgramId(): PublicKey = PublicKey(Constants.MAIN_NET_PUBLIC_KEY)
}