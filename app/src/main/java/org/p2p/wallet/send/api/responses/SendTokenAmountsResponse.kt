package org.p2p.wallet.send.api.responses

import com.google.gson.annotations.SerializedName
import java.math.BigInteger
import org.p2p.core.crypto.Base58String

class SendTokenAmountsResponse(
    @SerializedName("address")
    val mintAddress: Base58String,
    @SerializedName("amount")
    val amount: BigInteger
)
