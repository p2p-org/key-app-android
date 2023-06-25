package org.p2p.wallet.jupiter.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base64String

data class CreateSwapTransactionResponse(
    @SerializedName("swapTransaction")
    val versionedSwapTransaction: Base64String
)
