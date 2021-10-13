package com.p2p.wallet.swap.orca.repository

import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.model.AccountBalance
import com.p2p.wallet.swap.orca.model.OrcaPool
import com.p2p.wallet.swap.orca.model.OrcaSwapRequest
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey

interface OrcaSwapRepository {
    suspend fun loadPools(swapProgramId: String): List<OrcaPool>
    suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance
    suspend fun swap(
        account: Account,
        request: OrcaSwapRequest,
        accountA: Token.Active?,
        associatedAddress: PublicKey,
        shouldCreateAssociatedInstruction: Boolean
    ): String
}