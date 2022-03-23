package org.p2p.solanaj.kits.transaction.network.meta

import com.google.gson.annotations.SerializedName

data class MetaResponse(
    @SerializedName("fee")
    val fee: Long,

    @SerializedName("innerInstructions")
    val innerInstructions: List<InnerInstructionDetailsResponse>?,

    @SerializedName("postTokenBalances")
    val postTokenBalances: List<TokenBalanceResponse>?,

    @SerializedName("preTokenBalances")
    val preTokenBalances: List<TokenBalanceResponse>
)
