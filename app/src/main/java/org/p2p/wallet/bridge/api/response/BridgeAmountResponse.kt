package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.wrapper.eth.EthAddress

class BridgeAmountResponse(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("usd_amount")
    val usdAmount: String,
    @SerializedName("chain")
    val chain: String?,
    @SerializedName("token")
    val token: EthAddress?,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("name")
    val tokenName: String,
    @SerializedName("decimals")
    val decimals: Int,
)
