package org.p2p.solanaj.kits.transaction.network.meta

import com.google.gson.annotations.SerializedName

data class InstructionErrorResponse(
    @SerializedName("InstructionError")
    val instructionError: List<Any>
)
