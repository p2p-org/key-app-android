package org.p2p.wallet.claim.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.claim.model.HexTransaction

class BridgeBundleResponse(
    @SerializedName("bundleId")
    val bundleId: String,
    @SerializedName("userWallet")
    val userWallet: EthAddress,
    @SerializedName("recipient")
    val recipient: SolAddress,
    @SerializedName("token")
    val erc20TokenAddress: EthAddress,
    @SerializedName("expiresAt")
    val expiresAt: String? = null,
    @SerializedName("transactions")
    val transactions: List<HexTransaction>,
    @SerializedName("signatures")
    val signatures: List<HexTransaction>? = null,
    @SerializedName("fees")
    val fees: BundleFeeResponse

)
