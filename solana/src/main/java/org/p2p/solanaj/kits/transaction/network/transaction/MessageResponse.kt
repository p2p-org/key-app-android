package org.p2p.solanaj.kits.transaction.network.transaction

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse

data class MessageResponse(
    @SerializedName("accountKeys")
    val accountKeys: List<AccountKeysResponse>,

    @SerializedName("instructions")
    val instructions: List<InstructionResponse>,

    @SerializedName("recentBlockhash")
    private val recentBlockhash: String
)
