package org.p2p.ethereumkit.external.model

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.wrapper.eth.EthAddress

data class EthTokenMetadata(
    val contractAddress: EthAddress,
    val mintAddress: String,
    val balance: BigInteger,
    val decimals: Int,
    val logoUrl: String?,
    val tokenName: String,
    val symbol: String,
    var price: BigDecimal? = null
)
