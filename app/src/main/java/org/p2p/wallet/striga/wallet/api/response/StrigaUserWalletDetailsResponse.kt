package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap

/**
 * @param createdAt - example 2022-07-22T12:39:56.835Z
 */
data class StrigaUserWalletDetailsResponse(
    @SerializedName("walletId")
    val walletId: String,
    @SerializedName("accounts")
    val accountCurrencyToDetails: LinkedTreeMap<String, StrigaUserWalletAccountResponse>,
    @SerializedName("rootFiatCurrency")
    val rootFiatCurrency: String,
    @SerializedName("syncedOwnerId")
    val syncedOwnerId: String,
    @SerializedName("ownerType")
    val ownerType: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("comment")
    val comment: String
)
