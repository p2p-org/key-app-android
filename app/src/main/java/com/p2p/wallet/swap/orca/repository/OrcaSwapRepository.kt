package com.p2p.wallet.swap.orca.repository

import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.orca.model.OrcaSwapRequest
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.TokenAccountBalance

interface OrcaSwapRepository {
    suspend fun loadPoolInfoList(swapProgramId: String): List<Pool.PoolInfo>
    suspend fun loadTokenBalance(publicKey: PublicKey): TokenAccountBalance
    suspend fun swap(
        path: DerivationPath,
        keys: List<String>,
        request: OrcaSwapRequest,
        accountA: Token.Active?,
        accountB: Token.Active?
    ): String
}