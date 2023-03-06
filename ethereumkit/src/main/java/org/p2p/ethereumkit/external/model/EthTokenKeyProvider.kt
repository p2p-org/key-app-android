package org.p2p.ethereumkit.external.model

import org.p2p.ethereumkit.internal.models.EthAddress
import java.math.BigInteger

data class EthTokenKeyProvider(
    val publicKey: EthAddress,
    val privateKey: BigInteger
)
