package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

class FreeFeeLimitsResponse(
    @SerializedName("authority")
    val authority: ByteArray,
    @SerializedName("limits")
    val limits: LimitsResponse,
    @SerializedName("processed_fee")
    val processedFee: ProcessedFeeResponse
)

class LimitsResponse(
    @SerializedName("use_free_fee")
    val useFreeFee: Boolean,
    @SerializedName("max_amount")
    val maxAmount: BigInteger,
    @SerializedName("max_count")
    val maxCount: Int,
    @SerializedName("period")
    val period: PeriodsResponse
)

class PeriodsResponse(
    @SerializedName("secs")
    val secs: BigInteger,
    @SerializedName("nanos")
    val nanos: BigInteger
)

class ProcessedFeeResponse(
    @SerializedName("total_amount")
    val totalAmount: BigInteger,
    @SerializedName("count")
    val count: Int
)
