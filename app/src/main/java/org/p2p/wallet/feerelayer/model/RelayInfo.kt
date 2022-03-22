package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.PublicKey
import java.math.BigInteger

class RelayInfo(
    val minimumTokenAccountBalance: BigInteger,
    val minimumRelayAccountBalance: BigInteger,
    val feePayerAddress: PublicKey,
    val lamportsPerSignature: BigInteger
)
