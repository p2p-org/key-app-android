package org.p2p.wallet.solend.repository

import org.p2p.wallet.solend.model.SolendMarketInfo
import org.p2p.wallet.solend.model.SolendUserDeposit

interface SolendDepositsRepository {
    suspend fun getDeposits(): List<SolendUserDeposit>
    suspend fun getSolendMarketInfo(tokens: List<String>): List<SolendMarketInfo>
}
