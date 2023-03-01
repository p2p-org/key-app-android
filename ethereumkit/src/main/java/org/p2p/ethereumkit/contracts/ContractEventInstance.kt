package org.p2p.ethereumkit.contracts

import org.p2p.ethereumkit.models.EthAddress

open class ContractEventInstance(val contractAddress: EthAddress) {

    open fun tags(userAddress: EthAddress): List<String> = listOf()

}
