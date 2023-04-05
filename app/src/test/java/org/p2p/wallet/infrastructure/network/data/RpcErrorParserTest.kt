package org.p2p.wallet.infrastructure.network.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.p2p.wallet.utils.toJsonObject

class RpcErrorParserTest {
    private val gson = Gson()

    @Language("JSON")
    private val validInstructionCustomErrorJson: JsonObject = """
        {
            "err": {
                "InstructionError": [
                    3,
                    {
                      "Custom": 6022
                    }
                ]
            }
      }"""
        .let(gson::toJsonObject)

    @Language("JSON")
    private val validInstructionOtherErrorJson: JsonObject = """
        {
            "err": {
                "InstructionError": [
                  3,
                  "Other"
                ]
            }
        }"""
        .let(gson::toJsonObject)

    @Language("JSON")
    private val invalidInstructionErrorJson: JsonObject = """
        {
            "err": {
                "InstructionError": [3]
            }
        }
        """
        .let(gson::toJsonObject)

    @Language("JSON")
    private val validInstructionAccountInUseErrorJson: JsonObject = """
        {
          "err": "AccountInUse"
        }
"""
        .let(gson::toJsonObject)

    @Test
    fun `given valid Custom instruction error json when parse then return valid object`() {
        // WHEN
        val instructionCustomError = RpcError.from(validInstructionCustomErrorJson["err"])

        // THEN
        val actualError = assertThat(instructionCustomError)
            .isNotNull()
            .isInstanceOf(RpcError.InstructionError::class)

        actualError.prop(RpcError.InstructionError::instructionIndex)
            .isEqualTo(3)
        actualError.prop(RpcError.InstructionError::instructionErrorType)
            .isInstanceOf(InstructionErrorType.Custom::class)
            .prop(InstructionErrorType.Custom::programErrorId)
            .isEqualTo(6022L)
    }

    @Test
    fun `given invalid custom instruction error json when parse then null is returned`() {
        // WHEN
        val instructionCustomError = RpcError.from(invalidInstructionErrorJson["err"].asJsonObject)
        // THEN
        assertThat(instructionCustomError).isNull()
    }

    @Test
    fun `given valid AccountInUse instruction error json when parse then success`() {
        // WHEN
        val instructionCustomError = RpcError.from(validInstructionAccountInUseErrorJson["err"])
        // THEN
        assertThat(instructionCustomError)
            .isNotNull()
            .isInstanceOf(RpcError.AccountInUse::class)
    }

    @Test
    fun `given valid Custom instruction error json when parse then success`() {
        // WHEN
        val instructionCustomError = RpcError.from(validInstructionOtherErrorJson["err"])
        // THEN
        val instructionError = assertThat(instructionCustomError)
            .isNotNull()
            .isInstanceOf(RpcError.InstructionError::class)

        val otherError = instructionError
            .prop(RpcError.InstructionError::instructionErrorType)
            .isInstanceOf(InstructionErrorType.Other::class)

        otherError.prop(InstructionErrorType.Other::name).isEqualTo("Other")
    }
}
