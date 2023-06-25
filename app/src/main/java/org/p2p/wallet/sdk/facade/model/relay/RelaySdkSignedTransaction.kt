package org.p2p.wallet.sdk.facade.model.relay

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

class RelaySdkSignedTransaction(
    @SerializedName("transaction")
    val transaction: Base58String
)
