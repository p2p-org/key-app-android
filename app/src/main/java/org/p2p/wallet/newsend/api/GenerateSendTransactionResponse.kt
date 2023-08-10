package org.p2p.wallet.newsend.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.Base64String
import org.p2p.core.utils.MillisSinceEpoch

data class GenerateSendTransactionResponse(
    @SerializedName("transaction")
    val transaction: Base64String,
    @SerializedName("blockhash")
    val blockhash: String,
    @SerializedName("expires_at")
    val expiresAt: MillisSinceEpoch,
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
    val usdAmount: AmountResponse
)

data class TokenAccountRentResponse(
    @SerializedName("source")
    val source: String,
    @SerializedName("amount")
    val usdAmount: AmountResponse
)

data class AmountResponse(
    @SerializedName("amount")
    val amount: BigInteger,
    @SerializedName("usd_amount")
    val usdAmount: BigDecimal,
    @SerializedName("mint")
    val tokenMint: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("decimals")
    val decimals: Int,
)
