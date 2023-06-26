package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaInitiateOnchainWithdrawalResponse(
    @SerializedName("challengeId")
    val challengeId: String,
    @SerializedName("dateExpires")
    val dateExpires: String,
    @SerializedName("transaction")
    val transaction: Transaction,
    @SerializedName("feeEstimate")
    val feeEstimate: FeeEstimate,
) {

    class Transaction(
        @SerializedName("syncedOwnerId")
        val syncedOwnerId: String,
        @SerializedName("sourceAccountId")
        val sourceAccountId: String,
        @SerializedName("parentWalletId")
        val parentWalletId: String,
        @SerializedName("currency")
        val currency: String,
        @SerializedName("amount")
        val amount: String,
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

    class FeeEstimate(
        @SerializedName("totalFee")
        val totalFee: String,
        @SerializedName("networkFee")
        val networkFee: String,
        @SerializedName("ourFee")
        val ourFee: String,
        @SerializedName("theirFee")
        val theirFee: String,
        @SerializedName("feeCurrency")
        val feeCurrency: String,
        @SerializedName("gasLimit")
        val gasLimit: String,
        @SerializedName("gasPrice")
        val gasPrice: String,
    )
}
