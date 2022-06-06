package org.p2p.wallet.swap.parser

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse

@RunWith(JUnit4::class)
class TransactionParserTest {

    @Test
    fun testTransactionParser() {
        val jsonFileName = "transactionJson.json"
        val jsonFile = TestHelper.loadJsonAsString(jsonFileName)
        val transaction = TestHelper.convertJsonToModel(jsonFile, ConfirmedTransactionRootResponse::class.java)
    }
}
