package org.p2p.ethereumkit.internal.contracts

import org.p2p.core.wrapper.eth.EthAddress

open class ContractEventInstance(val contractAddress: EthAddress) {

    open fun tags(userAddress: EthAddress): List<String> = listOf()

}
