package org.p2p.solanaj.model.core

class AccountMeta(
    val publicKey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
)