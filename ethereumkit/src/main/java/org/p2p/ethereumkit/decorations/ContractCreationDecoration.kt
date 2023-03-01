package org.p2p.ethereumkit.decorations

class ContractCreationDecoration : TransactionDecoration() {

    override fun tags(): List<String> = listOf("contractCreation")

}
