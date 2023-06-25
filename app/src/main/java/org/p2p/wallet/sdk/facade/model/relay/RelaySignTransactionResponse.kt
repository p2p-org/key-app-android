package org.p2p.wallet.sdk.facade.model.relay

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance

data class RelaySignTransactionResponse(
    @SerializedName("transaction")
    val transaction: String
) {
    val transactionAsBase58: Base58String
        get() = transaction.toBase58Instance()
}
