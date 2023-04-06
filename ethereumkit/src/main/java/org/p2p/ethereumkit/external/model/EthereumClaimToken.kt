package org.p2p.ethereumkit.external.model

import org.p2p.core.wrapper.eth.EthAddress

data class EthereumClaimToken(
    val contractAddress: EthAddress,
    val isClaiming: Boolean,
)
