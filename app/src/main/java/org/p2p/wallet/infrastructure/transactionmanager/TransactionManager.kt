package org.p2p.wallet.infrastructure.transactionmanager

interface TransactionManager {
    fun launch()
    fun getTransactionStateFlow()
}
