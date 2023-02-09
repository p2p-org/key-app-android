package org.p2p.wallet.sdk.facade.model.relay

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

data class RelaySignTransactionResponse(
    @SerializedName("transaction")
    val transaction: String
) {
    val transactionAsBase58: Base58String
        get() = transaction.toBase58Instance()
}
