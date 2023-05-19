package org.p2p.ethereumkit.external.model

import java.math.BigDecimal
import org.p2p.core.wrapper.eth.EthAddress

data class EthereumClaimToken(
    val bundleId: String,
    val contractAddress: EthAddress,
    val tokenAmount: BigDecimal?,
    val fiatAmount: BigDecimal?,
    val isClaiming: Boolean,
)
