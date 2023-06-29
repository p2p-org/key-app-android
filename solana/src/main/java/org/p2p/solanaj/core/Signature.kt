package org.p2p.solanaj.core

import org.p2p.core.utils.Base58Utils

data class Signature(
    val publicKey: PublicKey,
    val signature: String
) {

    constructor(publicKey: PublicKey, signature: ByteArray?) : this(
        publicKey, if (signature == null) "" else Base58Utils.encode(signature)
    )
}
