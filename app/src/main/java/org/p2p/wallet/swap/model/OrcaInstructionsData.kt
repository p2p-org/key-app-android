package org.p2p.wallet.swap.model

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
@Deprecated("Old swap")
data class OrcaInstructionsData(
    val account: PublicKey,
    val instructions: MutableList<TransactionInstruction> = mutableListOf(),
    val closeInstructions: List<TransactionInstruction> = listOf(),
    val signers: List<Account> = emptyList()
)
