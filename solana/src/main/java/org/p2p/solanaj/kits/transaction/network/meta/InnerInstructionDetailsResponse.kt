package org.p2p.solanaj.kits.transaction.network.meta

import com.google.gson.annotations.SerializedName

data class InnerInstructionDetailsResponse(
    @SerializedName("index")
    val instructionIndex: Int,

    @SerializedName("instructions")
    val instructions: List<InstructionResponse>
)
