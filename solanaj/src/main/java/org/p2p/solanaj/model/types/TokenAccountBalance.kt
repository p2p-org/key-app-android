package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class TokenAccountBalance(
    @SerializedName("value")
    val value: Balance
) : RpcResultObject() {

    data class Balance(
        @SerializedName("amount")
        val amount: String,

        @SerializedName("decimals")
        val decimals: Int
    )

    val amount: BigInteger
        get() = BigInteger(value.amount)
}
