package org.p2p.solanaj.kits

import org.p2p.solanaj.model.core.Account
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.model.core.TransactionInstruction

class AccountInstructions(
    val account: PublicKey,
    val instructions: MutableList<TransactionInstruction> = mutableListOf(),
    val cleanupInstructions: List<TransactionInstruction> = listOf(),
    val signers: List<Account> = listOf(),
    val newWalletPubkey: String? = null,
    val secretKey: ByteArray? = null
)