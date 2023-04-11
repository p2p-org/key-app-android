package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

class TransactionFeeLimitsResponse(
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
    @SerializedName("max_fee_amount")
    val maxFeeAmount: BigInteger?,
    @SerializedName("max_fee_count")
    val maxFeeCount: Int?,
    @SerializedName("max_amount")
    val maxAmount: BigInteger,
    @SerializedName("max_count")
    val maxCount: Int,
    @SerializedName("max_token_account_creation_amount")
    val maxAccountCreationAmount: BigInteger?,
    @SerializedName("max_token_account_creation_count")
    val maxAccountCreationCount: Int?
)

class PeriodsResponse(
    @SerializedName("secs")
    val secs: BigInteger,
    @SerializedName("nanos")
    val nanos: BigInteger
)

class ProcessedFeeResponse(
    @SerializedName("total_fee_amount")
    val totalFeeAmount: BigInteger?,
    @SerializedName("total_rent_amount")
    val totalRentAmount: BigInteger?,
    @SerializedName("total_amount")
    val totalAmount: BigInteger,
    @SerializedName("count")
    val count: Int,
    @SerializedName("rent_count")
    val rentCount: Int?,
    @SerializedName("fee_count")
    val feeCount: Int?
)
