package org.p2p.wallet.infrastructure.network.data

import com.google.gson.JsonElement

private const val KEY_CUSTOM_INSTRUCTION_ERROR_ID = "Custom"

sealed interface InstructionErrorType {
    companion object {
        // can be { "Custom" : 1111 } or just plain "SomeError"
        fun from(instructionErrorType: JsonElement): InstructionErrorType {
            val isCustom = instructionErrorType.isJsonPrimitive
            return if (isCustom) {
                Custom(programErrorId = instructionErrorType.asJsonObject[KEY_CUSTOM_INSTRUCTION_ERROR_ID].asLong)
            } else {
                Other(name = instructionErrorType.asString)
            }
        }
    }

    data class Custom(val programErrorId: Long) : InstructionErrorType
    class Other(val name: String) : InstructionErrorType
}
