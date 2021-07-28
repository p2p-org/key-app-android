package org.p2p.solanaj.model.core

class TransactionInstruction(
    val programId: PublicKey,
    val keys: List<AccountMeta>,
    val data: ByteArray
)