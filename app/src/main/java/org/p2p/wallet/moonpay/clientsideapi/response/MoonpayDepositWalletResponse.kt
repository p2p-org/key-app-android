package org.p2p.wallet.moonpay.clientsideapi.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class MoonpaySellTransactionDepositWalletResponse(
    @SerializedName("id")
    val transactionId: String,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("externalTransactionId")
    val externalTransactionId: String,
    @SerializedName("externalCustomerId")
    val externalCustomerId: String?,
    @SerializedName("depositWallet")
    val depositWallet: MoonpayDepositWalletResponse,
)

data class MoonpayDepositWalletResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("walletAddress")
    val walletAddress: Base58String,
    @SerializedName("walletAddressTag")
    val walletAddressTag: String,
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("currencyId")
    val currencyId: String
)
