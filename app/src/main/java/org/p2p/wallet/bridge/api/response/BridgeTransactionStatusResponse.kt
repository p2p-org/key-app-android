package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.token.SolAddress
import org.p2p.core.utils.MillisSinceEpoch

class BridgeTransactionStatusResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("user_wallet")
    val userWallet: SolAddress,
    @SerializedName("recipient")
    val recipient: SolAddress,
    @SerializedName("amount")
    val amount: BridgeAmountResponse?,
    @SerializedName("fees")
    val fees: BridgeSendFeesResponse,
    @SerializedName("status")
    val status: BridgeSendStatusResponse,
    @SerializedName("created")
    val dateCreated: MillisSinceEpoch
)
