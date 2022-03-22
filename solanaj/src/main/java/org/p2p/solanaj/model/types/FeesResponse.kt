package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

data class FeesResponse(
    @SerializedName("value")
    val value: FeesValue
)

data class FeesValue(
    @SerializedName("feeCalculator")
    val feeCalculator: FeeCalculatorResponse
)

data class FeeCalculatorResponse(
    @SerializedName("lamportsPerSignature")
    val lamportsPerSignature: Long
)
