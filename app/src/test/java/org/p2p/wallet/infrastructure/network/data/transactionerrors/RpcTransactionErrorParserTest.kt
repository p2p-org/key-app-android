package org.p2p.wallet.infrastructure.network.data.transactionerrors

import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.isNotInstanceOf
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.stream.JsonReader
import org.junit.Test
import java.io.StringReader
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.core.network.data.transactionerrors.RpcTransactionErrorTypeAdapter
import org.p2p.core.network.data.transactionerrors.RpcTransactionInstructionErrorParser
import org.p2p.core.network.data.transactionerrors.TransactionInstructionError

class RpcTransactionErrorParserTest {
    private val gson = Gson()

    private val rpcErrorTypeAdapter = RpcTransactionErrorTypeAdapter(
        RpcTransactionInstructionErrorParser()
    )

    private val transactionErrorsArray: JsonArray = javaClass.classLoader
        .getResource("transaction_errors.json")
        .readText()
        .let { gson.fromJson(it, JsonArray::class.java) }

    private fun getTransactionErrorValueAt(index: Int) =
        transactionErrorsArray.get(index)
            .asJsonObject
            .get("err")

    @Test
    fun `given valid transactions errors json when parse then return valid parsed errors`() {
        // WHEN
        val allErrorsFromFile: List<RpcTransactionError> = transactionErrorsArray
            .mapIndexed { index, _ -> getTransactionErrorValueAt(index).toString() }
            .map { rpcErrorTypeAdapter.read(JsonReader(StringReader(it))) }

        assertThat(allErrorsFromFile)
            .each {
                it.isNotInstanceOf(RpcTransactionError.Unknown::class)
                it.transform { it as? RpcTransactionError.InstructionError }
                    .given { it?.instructionErrorType !is TransactionInstructionError.Unknown }
            }
    }
}
