package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class StrigaUserWalletAccountResponse(
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("parentWalletId")
    val parentWalletId: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("ownerId")
    val ownerId: String,
    @SerializedName("rootFiatCurrency")
    val rootFiatCurrency: String,
    @SerializedName("ownerType")
    val ownerType: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("availableBalance")
    val availableBalance: AvailableBalanceResponse,
    @SerializedName("linkedCardId")
    val linkedCardId: String,
    @SerializedName("linkedBankAccountId")
    val linkedBankAccountId: String,
    @SerializedName("status")
    val status: String
) {
    /**
     * @param currencyUnits can be cents, or satoshis or smth else
     */
    data class AvailableBalanceResponse(
        @SerializedName("amount")
        val amount: BigInteger,
        @SerializedName("currency")
        val currencyUnits: String
    )
}
