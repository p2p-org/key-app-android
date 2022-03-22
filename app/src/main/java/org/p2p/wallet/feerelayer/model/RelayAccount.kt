package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.PublicKey
import java.math.BigInteger

class RelayAccount(
    val publicKey: PublicKey,
    val isCreated: Boolean,
    val balance: BigInteger?
)
