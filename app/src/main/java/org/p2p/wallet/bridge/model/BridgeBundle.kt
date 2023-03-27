package org.p2p.wallet.bridge.model

import kotlin.time.Duration.Companion.seconds
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.bridge.claim.model.ClaimStatus

data class BridgeBundle(
    val bundleId: String,
    val userWallet: EthAddress,
    val recipient: SolAddress,
    val resultAmount: BridgeFee,
    val token: EthAddress?,
    val compensationDeclineReason: String,
    val expiresAt: Long,
    val transactions: List<HexString>,
    var signatures: List<Signature>,
    val fees: BridgeBundleFees,
    val status: ClaimStatus? = null
) {
    fun getExpirationDateInMillis(): Long {
        return expiresAt.seconds.inWholeMilliseconds.toZonedDateTime().dateMilli()
    }
}
