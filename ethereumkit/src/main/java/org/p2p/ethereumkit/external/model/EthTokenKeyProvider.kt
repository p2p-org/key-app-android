package org.p2p.ethereumkit.external.model

import org.p2p.core.wrapper.eth.EthAddress
import java.math.BigInteger

internal data class EthTokenKeyProvider(
    val publicKey: EthAddress,
    val privateKey: BigInteger
)
