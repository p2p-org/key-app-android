package org.p2p.wallet.send.api.responses

import com.google.gson.annotations.SerializedName

data class SendServiceFreeLimitsResponse(
    @SerializedName("network_fee")
    val networkFee: NetworkFeeLimitsResponse,
    @SerializedName("token_account_rent")
    val tokenAccountRent: TokenAccountRentLimitsResponse
) {
    data class NetworkFeeLimitsResponse(
        @SerializedName("remaining_amount")
        val remainingAmount: Long,
        @SerializedName("remaining_transactions")
        val remainingTransactions: Long
    )

    data class TokenAccountRentLimitsResponse(
        @SerializedName("remaining_amount")
        val remainingAmount: Long,
        @SerializedName("remaining_transactions")
        val remainingTransactions: Long
    )
}
