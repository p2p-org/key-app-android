package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName

/**
 * @param amountInUnits The amount denominated in the smallest divisible unit of the sending currency.
 * For example: cents or satoshis
 */
class StrigaOnchainWithdrawalFeeRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("sourceAccountId")
    val sourceAccountId: String,
    @SerializedName("whitelistedAddressId")
    val whitelistedAddressId: String,
    @SerializedName("amount")
    val amountInUnits: String,
)
