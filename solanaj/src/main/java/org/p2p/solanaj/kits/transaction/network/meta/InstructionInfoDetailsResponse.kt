package org.p2p.solanaj.kits.transaction.network.meta

import com.google.gson.annotations.SerializedName

data class InstructionInfoDetailsResponse(
    @SerializedName("info")
    val info: InformationResponse,

    @SerializedName("type")
    val type: String?
)

data class InformationResponse(
    @SerializedName("owner")
    val owner: String?,
    @SerializedName("account")
    val account: String?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("destination")
    val destination: String?,

    // create account fields
    @SerializedName("lamports")
    val lamports: Float?,
    @SerializedName("newAccount")
    val newAccount: String?,
    @SerializedName("space")
    val space: Long?,

    // initialize account fields
    @SerializedName("mint")
    val mint: String?,
    @SerializedName("rentSysvar")
    val rentSysvar: String?,

    // approve fields
    @SerializedName("amount")
    val amount: String?,
    @SerializedName("delegate")
    val delegate: String?,

    // transfer
    @SerializedName("authority")
    val authority: String?,
    @SerializedName("wallet")
    val wallet: String?, // spl-associated-token-account

    @SerializedName("tokenAmount")
    val tokenAmount: TokenAccountBalanceResponse?,
)
