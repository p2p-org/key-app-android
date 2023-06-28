package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaInitWithdrawalResponse(
    @SerializedName("challengeId")
    val challengeId: String,
    @SerializedName("dateExpires")
    val dateExpires: String,
    @SerializedName("transaction")
    val transaction: WithdrawalTransactionResponse,
    @SerializedName("feeEstimate")
    val feeEstimate: StrigaOnchainWithdrawalFeeResponse,
) {
    class WithdrawalTransactionResponse(
        @SerializedName("syncedOwnerId")
        val syncedOwnerId: String,
        @SerializedName("sourceAccountId")
        val sourceAccountId: String,
        @SerializedName("parentWalletId")
        val parentWalletId: String,
        @SerializedName("currency")
        val currency: String,
        @SerializedName("amount")
        val amountInUnits: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("txType")
        val txType: String,
        @SerializedName("blockchainDestinationAddress")
        val blockchainDestinationAddress: String,
        @SerializedName("blockchainNetwork")
        val blockchainNetwork: StrigaBlockchainNetworkResponse,
        @SerializedName("transactionCurrency")
        val transactionCurrency: String,
    )
}
