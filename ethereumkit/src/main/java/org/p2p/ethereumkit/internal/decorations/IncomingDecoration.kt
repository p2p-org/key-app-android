package org.p2p.ethereumkit.internal.decorations

import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.TransactionTag
import java.math.BigInteger

class IncomingDecoration(
    val from: EthAddress,
    val value: BigInteger
) : TransactionDecoration() {

    override fun tags(): List<String> =
        listOf(TransactionTag.EVM_COIN, TransactionTag.EVM_COIN_INCOMING, TransactionTag.INCOMING)

}
