package org.p2p.wallet.solend.data

import org.p2p.wallet.sdk.facade.model.SolendCollateralAccountResponse
import org.p2p.wallet.sdk.facade.model.SolendUserDepositResponse

interface SolendUserDepositsRepository {
    suspend fun getCollateralAccounts(): List<SolendCollateralAccountResponse>
    suspend fun getDeposits(): List<SolendUserDepositResponse>
}
