package org.p2p.ethereumkit.internal.decorations

class ContractCreationDecoration : TransactionDecoration() {

    override fun tags(): List<String> = listOf("contractCreation")

}
