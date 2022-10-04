package org.p2p.wallet.solend.repository

import org.p2p.wallet.solend.model.SolendDepositToken

interface SolendDepositsRepository {
    suspend fun getUserDeposits(tokenSymbols: List<String>): List<SolendDepositToken>
}
