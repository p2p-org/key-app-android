package org.p2p.ethereumkit.external.model

import org.p2p.ethereumkit.internal.models.EthAddress
import java.math.BigDecimal
import java.math.BigInteger

data class EthTokenMetadata(
    val contractAddress: EthAddress,
    val balance: BigInteger,
    val decimals: Int,
    val logoUrl: String,
    val tokenName: String,
    val symbol: String,
    var price: BigDecimal = BigDecimal.ZERO
)
