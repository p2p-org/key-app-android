package org.p2p.solanaj.kits.transaction.network.meta

import com.google.gson.annotations.SerializedName

data class InstructionResponse(
    @SerializedName("parsed")
    val parsed: InstructionInfoDetailsResponse?,

    @SerializedName("program")
    val program: String?,

    @SerializedName("programId")
    val programId: String?,

    @SerializedName("accounts")
    val accounts: List<String>,

    @SerializedName("data")
    val data: String?
)
