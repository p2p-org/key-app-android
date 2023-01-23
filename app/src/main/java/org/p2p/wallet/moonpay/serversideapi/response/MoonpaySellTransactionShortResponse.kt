package org.p2p.wallet.moonpay.serversideapi.response

import com.google.gson.annotations.SerializedName

data class MoonpaySellTransactionShortResponse(
    @SerializedName("id")
    val transactionId: String,
    // "2020-09-24T07:18:27.469Z"
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("status")
    val status: SellTransactionStatus,
    @SerializedName("baseCurrencyAmount")
    val tokenAmount: Double,
    @SerializedName("feeAmount")
    val feeAmount: Double?,
    @SerializedName("extraFeeAmount")
    val extraFeeAmount: Double?,
    @SerializedName("quoteCurrencyAmount")
    val fiatAmount: Double?,
    @SerializedName("usdRate")
    val usdRate: Double,
    @SerializedName("eurRate")
    val eurRate: Double,
    @SerializedName("gbpRate")
    val gbpRate: Double,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("bankAccountId")
    val bankAccountId: String,
    @SerializedName("externalTransactionId")
    val externalTransactionId: String?,
    @SerializedName("failureReason")
    val failureReason: SellTransactionFailureReason?,
    @SerializedName("externalCustomerId")
    val externalCustomerId: String?,
    @SerializedName("country")
    val countryAbbreviation: String,
    @SerializedName("state")
    val stateAbbreviation: String?
)
