package com.p2p.wallet.swap.repository

import com.p2p.wallet.main.model.Token
import com.p2p.wallet.swap.model.AccountBalance
import com.p2p.wallet.swap.model.orca.OrcaPool
import com.p2p.wallet.swap.model.orca.OrcaSwapRequest
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey

interface OrcaSwapRepository {
    suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance
    suspend fun swap(
        account: Account,
        request: OrcaSwapRequest,
        accountA: Token.Active?,
        associatedAddress: PublicKey,
        shouldCreateAssociatedInstruction: Boolean
    ): String

    suspend fun sendAndWait(serializedTransaction: String, onConfirmed: () -> Unit)
}