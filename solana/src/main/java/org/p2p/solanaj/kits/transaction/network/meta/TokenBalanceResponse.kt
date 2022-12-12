package org.p2p.solanaj.kits.transaction.network.meta

import com.google.gson.annotations.SerializedName

data class TokenBalanceResponse(
    @SerializedName("accountIndex")
    val accountIndex: Int,

    @SerializedName("mint")
    val mint: String?,

    @SerializedName("uiTokenAmount")
    val uiTokenAmountDetails: TokenAccountBalanceResponse?,
)

data class TokenAccountBalanceResponse(
    @SerializedName("uiAmount")
    val uiAmount: Float?,

    @SerializedName("uiAmountString")
    val uiAmountString: String?,

    @SerializedName("amount")
    val amount: String,

    @SerializedName("decimals")
    val decimals: Float?,
)
