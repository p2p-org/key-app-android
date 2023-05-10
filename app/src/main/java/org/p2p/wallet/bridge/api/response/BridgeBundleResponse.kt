package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.token.SolAddress
import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.wallet.bridge.claim.model.ClaimStatus

class BridgeBundleResponse(
    @SerializedName("bundle_id")
    val bundleId: String,
    @SerializedName("user_wallet")
    val userWallet: EthAddress,
    @SerializedName("recipient")
    val recipient: SolAddress,
    @SerializedName("token")
    val erc20TokenAddress: EthAddress? = null,
    @SerializedName("result_amount")
    val resultAmount: BridgeAmountResponse? = null,
    @SerializedName("compensation_decline_reason")
    val compensationDeclineReason: String? = null,
    @SerializedName("expires_at")
    val expiresAt: Long? = null,
    @SerializedName("transactions")
    val transactions: List<HexString>? = null,
    @SerializedName("signatures")
    var signatures: List<Signature>? = null,
    @SerializedName("fees")
    val fees: BridgeBundleFeesResponse? = null,
    @SerializedName("status")
    val status: ClaimStatus? = null,
    @SerializedName("claim_key")
    val claimKey: String? = null,
    @SerializedName("created")
    val dateCreated: MillisSinceEpoch
)
