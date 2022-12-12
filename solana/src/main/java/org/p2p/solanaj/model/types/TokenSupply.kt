package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

class TokenSupply(
    @SerializedName("context")
    val context: TokenSupplyContext,
    @SerializedName("value")
    val value: TokenSupplyValue
)

class TokenSupplyContext(
    @SerializedName("slot")
    val slot: Long
)

class TokenSupplyValue(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("uiAmount")
    val uiAmount: Double,
    @SerializedName("uiAmountString")
    val uiAmountString: String,
)
