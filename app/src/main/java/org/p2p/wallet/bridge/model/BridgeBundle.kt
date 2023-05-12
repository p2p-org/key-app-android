package org.p2p.wallet.bridge.model

import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import kotlin.time.Duration.Companion.seconds
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.bridge.claim.model.ClaimStatus
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.date.toZonedDateTime

@Parcelize
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
    val dateCreated: ZonedDateTime,
    var status: ClaimStatus? = null,
    var claimKey: String? = null
) : Parcelable {
    fun getExpirationDateInMillis(): Long {
        return expiresAt.seconds.inWholeMilliseconds.toZonedDateTime().dateMilli()
    }

    fun findToken(): EthAddress {
        return resultAmount.token ?: EthAddress(ERC20Tokens.ETH.contractAddress)
    }
}
