package org.p2p.ethereumkit.decorations

import org.p2p.ethereumkit.models.EthAddress
import org.p2p.ethereumkit.models.TransactionTag
import java.math.BigInteger

class IncomingDecoration(
    val from: EthAddress,
    val value: BigInteger
) : TransactionDecoration() {

    override fun tags(): List<String> =
        listOf(TransactionTag.EVM_COIN, TransactionTag.EVM_COIN_INCOMING, TransactionTag.INCOMING)

}
