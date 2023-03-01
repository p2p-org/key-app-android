package org.p2p.ethereumkit.model

import org.p2p.ethereumkit.models.EthAddress
import java.math.BigInteger

class EthTokenKeyProvider(
    val publicKey: EthAddress,
    val privateKey: BigInteger
)
