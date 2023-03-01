package org.p2p.ethereumkit.internal.model

import org.p2p.ethereumkit.internal.models.EthAddress
import java.math.BigInteger

class EthTokenKeyProvider(
    val publicKey: EthAddress,
    val privateKey: BigInteger
)
