package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.utils.Base58String

data class SignTransactionResponse(
    @SerializedName("signature")
    val signature: Base58String,
    @SerializedName("transaction")
    val transaction: Base64String,
)
