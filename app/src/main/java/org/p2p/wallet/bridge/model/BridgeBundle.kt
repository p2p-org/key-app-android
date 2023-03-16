package org.p2p.wallet.bridge.model

import com.google.gson.annotations.SerializedName
import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.core.wrapper.HexString

data class BridgeBundle(
    @SerializedName("bundle_id")
    val bundleId: String,
    @SerializedName("user_wallet")
    val userWallet: EthAddress,
    @SerializedName("recipient")
    val recipient: SolAddress,
    @SerializedName("token")
    val token: EthAddress,
    @SerializedName("expires_at")
    val expiresAt: Long,
    @SerializedName("transactions")
    val transactions: List<HexString>,
    @SerializedName("signatures")
    val signatures: List<HexString>,
    @SerializedName("fees")
    val fees: BridgeBundleFees,
)
