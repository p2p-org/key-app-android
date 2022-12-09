package org.p2p.wallet.moonpay.serversideapi.response

import com.google.gson.annotations.SerializedName

data class MoonpaySellTransactionResponse(
    @SerializedName("id")
    val transactionId: String,
    // "2020-09-24T07:18:27.469Z"
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("bankAccountId")
    val bankAccountId: String,
    @SerializedName("externalTransactionId")
    val externalTransactionId: String?,
    @SerializedName("failureReason")
    val failureReason: String?,
    @SerializedName("externalCustomerId")
    val externalCustomerId: String?,
    @SerializedName("country")
    val countryAbbreviation: String,
    @SerializedName("state")
    val stateAbbreviation: String
)
