package org.p2p.wallet.infrastructure.network.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.assertj.core.api.Assertions.assertThat
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
        assertThat(instructionCustomError)
            .isInstanceOfSatisfying(RpcError.InstructionError::class.java) { error ->
                assertThat(error.instructionIndex).isEqualTo(3)
            }
            .extracting { (it as RpcError.InstructionError).instructionErrorType }
            .isInstanceOfSatisfying(InstructionErrorType.Custom::class.java) { customInstructionError ->
                assertThat(customInstructionError).matches { it.programErrorId == 6022L }
            }
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
            .isInstanceOf(RpcError.AccountInUse::class.java)
    }

    @Test
    fun `given valid Custom instruction error json when parse then success`() {
        // WHEN
        val instructionCustomError = RpcError.from(validInstructionOtherErrorJson["err"])
        // THEN
        assertThat(instructionCustomError)
            .isNotNull
            .isInstanceOfSatisfying(RpcError.InstructionError::class.java) { error ->
                assertThat(error.instructionErrorType)
                    .isInstanceOf(InstructionErrorType.Other::class.java)
                    .extracting { it as InstructionErrorType.Other }
                    .matches { it.name == "Other" }
            }
    }
}
