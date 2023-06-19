package org.p2p.solanaj.rpc

data class TransactionSimulationResult(
    val isSimulationSuccess: Boolean,
    val errorIfSimulationFailed: String? = null
)
