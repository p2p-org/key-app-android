package org.p2p.wallet.sdk.facade.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.BigInteger

class SolendMarketInformationResponse(
    @SerializedName("token_symbol")
    val tokenSymbol: String,
    @SerializedName("current_supply")
    val currentSupply: BigDecimal,
    @SerializedName("deposit_limit")
    val depositLimitAmount: BigInteger,
    @SerializedName("supply_interest")
    val supplyInterest: BigDecimal
)
