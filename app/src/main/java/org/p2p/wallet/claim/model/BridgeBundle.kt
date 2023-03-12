package org.p2p.wallet.claim.model

import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.models.EthAddress

data class BridgeBundle(
    val bundleId: String,
    val userWallet: EthAddress,
    val recipient: SolAddress,
    val token: EthAddress,
    val expiresAt: String,
    val transactions: List<String>,
    val signatures: List<String>,
    val fees: List<BundleFee>
)
