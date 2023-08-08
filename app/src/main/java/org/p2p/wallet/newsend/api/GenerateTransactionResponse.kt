package org.p2p.wallet.newsend.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.BigInteger

data class GenerateTransactionResponse(
    @SerializedName("transaction")
    val transaction: String,
    @SerializedName("blockhash")
    val blockhash: String,
    @SerializedName("expiresAt")
    val expiresAt: Long,
    @SerializedName("signature")
    val signature: String,
    @SerializedName("recipient_gets_amount")
    val recipientGetsAmount: AmountResponse,
    @SerializedName("total_amount")
    val totalAmount: AmountResponse,
    @SerializedName("network_fee")
    val networkFee: NetworkFeeResponse,
    @SerializedName("token_account_rent")
    val tokenAccountRent: TokenAccountRentResponse,
)

data class NetworkFeeResponse(
    @SerializedName("source")
    val source: String,
    @SerializedName("amount")
    val usd_amount: AmountResponse
)

data class TokenAccountRentResponse(
    @SerializedName("source")
    val source: String,
    @SerializedName("amount")
    val usd_amount: AmountResponse
)

data class AmountResponse(
    @SerializedName("amount")
    val amount: BigInteger,
    @SerializedName("usd_amount")
    val usd_amount: BigDecimal,
    @SerializedName("mint")
    val mint: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("decimals")
    val decimals: Int,
)
