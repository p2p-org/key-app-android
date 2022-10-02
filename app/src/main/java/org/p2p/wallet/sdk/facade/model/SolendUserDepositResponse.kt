package org.p2p.wallet.sdk.facade.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class SolendUserDepositResponse(
    @SerializedName("depositedAmount")
    val depositedAmount: BigDecimal,
    @SerializedName("symbol")
    val depositTokenSymbol: String
)
