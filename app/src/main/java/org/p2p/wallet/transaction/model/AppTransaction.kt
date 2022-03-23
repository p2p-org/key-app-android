package org.p2p.wallet.transaction.model

data class AppTransaction(
    val serializedTransaction: String,
    val sourceSymbol: String,
    val destinationSymbol: String,
    val isSimulation: Boolean
)
