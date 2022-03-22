package org.p2p.wallet.swap.api

import com.google.gson.annotations.SerializedName

data class OrcaPoolResponse(
    @SerializedName("account")
    val account: String,
    @SerializedName("authority")
    val authority: String,
    @SerializedName("nonce")
    val nonce: Int,
    @SerializedName("poolTokenMint")
    val poolTokenMint: String,
    @SerializedName("tokenAccountA")
    val tokenAccountA: String,
    @SerializedName("tokenAccountB")
    val tokenAccountB: String,
    @SerializedName("feeAccount")
    val feeAccount: String,
    @SerializedName("hostFeeAccount")
    val hostFeeAccount: String?,
    @SerializedName("feeNumerator")
    val feeNumerator: Long,
    @SerializedName("feeDenominator")
    val feeDenominator: Long,
    @SerializedName("ownerTradeFeeNumerator")
    val ownerTradeFeeNumerator: Long,
    @SerializedName("ownerTradeFeeDenominator")
    val ownerTradeFeeDenominator: Long,
    @SerializedName("ownerWithdrawFeeNumerator")
    val ownerWithdrawFeeNumerator: Long,
    @SerializedName("ownerWithdrawFeeDenominator")
    val ownerWithdrawFeeDenominator: Long,
    @SerializedName("hostFeeNumerator")
    val hostFeeNumerator: Long,
    @SerializedName("hostFeeDenominator")
    val hostFeeDenominator: Long,
    @SerializedName("tokenAName")
    val tokenAName: String,
    @SerializedName("tokenBName")
    val tokenBName: String,
    @SerializedName("curveType")
    val curveType: String,
    @SerializedName("programVersion")
    val programVersion: Int?,
    @SerializedName("amp")
    val amp: Long?,
    @SerializedName("deprecated")
    val deprecated: Boolean?
)
