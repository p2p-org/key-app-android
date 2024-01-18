package org.p2p.wallet.send.api.responses

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.wallet.send.model.send_service.NetworkFeeSource

data class SendGeneratedTransactionResponse(
    @SerializedName("transaction")
    val transaction: Base64String,
    @SerializedName("blockhash")
    val blockhash: Base58String,
    @SerializedName("expires_at")
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
    val tokenAccountRent: NetworkFeeResponse?,
    @SerializedName("token_2022_transfer_fee")
    val token2022TransferFee: NetworkFeeResponse?,
) {

    data class AmountResponse(
        @SerializedName("amount")
        val amount: BigInteger,
        @SerializedName("usd_amount")
        val usdAmount: BigDecimal,
        @SerializedName("address")
        val address: Base58String,
        @SerializedName("symbol")
        val tokenSymbol: String,
        @SerializedName("name")
        val tokenName: String,
        @SerializedName("decimals")
        val decimals: Int,
        @SerializedName("logo_url")
        val logoUrl: String?,
        @SerializedName("coingecko_id")
        val coingeckoId: String?,
        @SerializedName("price")
        val price: Map<String, BigDecimal>,
    )

    data class NetworkFeeResponse(
        @SerializedName("source")
        val source: NetworkFeeSource,
        @SerializedName("amount")
        val amount: AmountResponse,
    )
}
