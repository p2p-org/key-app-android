package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigInteger
import org.p2p.core.token.SolAddress

class BridgeTransactionStatusResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("user_wallet")
    val userWallet: SolAddress,
    @SerializedName("recipient")
    val recipient: SolAddress,
    @SerializedName("amount")
    val amount: BigInteger,
    @SerializedName("fees")
    val fees: BridgeSendFeesResponse,
    @SerializedName("status")
    val status: BridgeSendStatusResponse
)
