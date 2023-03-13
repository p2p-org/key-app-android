package org.p2p.wallet.claim.model

import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.utils.HexString

data class BridgeBundle(
    val bundleId: String,
    val userWallet: EthAddress,
    val recipient: SolAddress,
    val token: EthAddress,
    val expiresAt: String,
    val transactions: List<HexString>,
    val signatures: List<HexString>,
    val fees: List<BundleFee>
)
