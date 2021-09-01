package org.p2p.solanaj.model.serumswap.model

import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.TransactionInstruction

data class SignersAndInstructions(
    val signers: List<Account>,
    val instructions: List<TransactionInstruction>
)