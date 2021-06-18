package com.p2p.wallet.swap.repository

import com.p2p.wallet.swap.model.SwapRequest
import com.p2p.wallet.token.model.Token
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.TokenAccountBalance

interface SwapRepository {
    suspend fun loadPoolInfoList(swapProgramId: String): List<Pool.PoolInfo>
    suspend fun loadTokenBalance(publicKey: PublicKey): TokenAccountBalance
    suspend fun swap(keys: List<String>, request: SwapRequest, accountA: Token?, accountB: Token?): String
}