package org.p2p.solanaj.core

class TransactionInstruction(
    val programId: PublicKey,
    val keys: List<AccountMeta>,
    val data: ByteArray
)