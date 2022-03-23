package org.p2p.solanaj.serumswap.model

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.TransactionInstruction

data class SignersAndInstructions(
    val signers: List<Account>,
    val instructions: List<TransactionInstruction>
)
