package org.p2p.wallet.solend.interactor

import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.repository.SolendDepositsRepository

class SolendDepositsInteractor(
    private val repository: SolendDepositsRepository
) {

    suspend fun getUserDeposits(tokenSymbols: List<String>): List<SolendDepositToken> =
        repository.getUserDeposits(tokenSymbols)
}
