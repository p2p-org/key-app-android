package org.p2p.token.service.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigInteger
import org.p2p.core.crypto.Base58String

internal class TokenAmountsResponse(
    @SerializedName("address")
    val mintAddress: Base58String,
    @SerializedName("amount")
    val amount: String // do not use BigInteger here, gson parses it as Hex
) {
    val amountLamports: BigInteger
        get() = amount.toBigInteger()
}
