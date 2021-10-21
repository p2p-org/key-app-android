package com.p2p.wallet.swap.model.orca

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction

data class OrcaInstructionsData(
    val account: PublicKey,
    val instructions: MutableList<TransactionInstruction>,
    val closeInstructions: List<TransactionInstruction> = listOf(),
    val signers: List<Account> = emptyList()
)
