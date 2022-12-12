package org.p2p.solanaj.core

class PreparedTransaction(
    val transaction: Transaction,
    val signers: List<Account>,
    val expectedFee: FeeAmount
)
