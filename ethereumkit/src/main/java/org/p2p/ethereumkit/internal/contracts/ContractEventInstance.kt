package org.p2p.ethereumkit.internal.contracts

import org.p2p.ethereumkit.internal.models.EthAddress

open class ContractEventInstance(val contractAddress: EthAddress) {

    open fun tags(userAddress: EthAddress): List<String> = listOf()

}
