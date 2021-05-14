package com.p2p.wallet.swap.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.rpc.types.TokenAccountBalance
import java.math.BigDecimal

interface SwapRepository {
    suspend fun loadPoolInfoList(swapProgramId: String): List<Pool.PoolInfo>
    suspend fun loadTokenBalance(publicKey: PublicKey): TokenAccountBalance


    suspend fun swap(
        keys: List<String>,
        pool: Pool.PoolInfo,
        source: String,
        destination: String,
        slippage: Double,
        amountIn: BigDecimal,
        balanceA: TokenAccountBalance,
        balanceB: TokenAccountBalance
    ): String
}