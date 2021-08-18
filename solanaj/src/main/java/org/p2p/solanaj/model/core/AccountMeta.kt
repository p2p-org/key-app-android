package org.p2p.solanaj.model.core

data class AccountMeta(
    val publicKey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
)