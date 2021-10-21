package com.p2p.wallet.swap.model.orca

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction

data class OrcaInstructionsData(
    val account: PublicKey,
    val instructions: List<TransactionInstruction>
)
