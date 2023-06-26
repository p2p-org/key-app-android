package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName

/**
 * @param userId The Id of the user who is sending this transaction
 * @param sourceAccountId The Id of the account to debit
 * @param whitelistedAddressId The Id of the whitelisted destination
 * @param amount The amount denominated in the smallest divisible unit of the sending currency.
 * For example: cents or satoshis
 */
class StrigaInitiateOnchainWithdrawalRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("sourceAccountId")
    val sourceAccountId: String,
    @SerializedName("whitelistedAddressId")
    val whitelistedAddressId: String,
    @SerializedName("amount")
    val amount: String,
)
