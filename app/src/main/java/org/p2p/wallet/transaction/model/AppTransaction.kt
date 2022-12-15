package org.p2p.wallet.transaction.model

import java.util.Date

data class AppTransaction(
    val serializedTransaction: String,
    val sourceSymbol: String,
    val destinationSymbol: String,
    val isSimulation: Boolean,
    val submissionDate: Date = Date()
)
