package org.p2p.wallet.sdk.facade.model.relay

import com.google.gson.annotations.SerializedName

class RelaySignTransactionResponse(
    @SerializedName("transaction")
    val transaction: String
)
