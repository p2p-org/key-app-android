package org.p2p.ethereumkit.internal.decorations

import org.p2p.ethereumkit.internal.contracts.ContractEventInstance
import org.p2p.ethereumkit.internal.contracts.ContractMethod
import org.p2p.ethereumkit.internal.contracts.EmptyMethod
import org.p2p.ethereumkit.internal.core.ITransactionDecorator
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.InternalTransaction
import java.math.BigInteger

class EthereumDecorator(private val address: EthAddress) : ITransactionDecorator {

    override fun decoration(from: EthAddress?, to: EthAddress?, value: BigInteger?, contractMethod: ContractMethod?, internalTransactions: List<InternalTransaction>, eventInstances: List<ContractEventInstance>): TransactionDecoration? {
        if (from == null || value == null) return null
        if (to == null) return ContractCreationDecoration()

        if (contractMethod != null && contractMethod is EmptyMethod) {
            if (from == address) {
                return OutgoingDecoration(to, value, to == address)
            }

            if (to == address) {
                return IncomingDecoration(from, value)
            }
        }

        return null
    }

}
