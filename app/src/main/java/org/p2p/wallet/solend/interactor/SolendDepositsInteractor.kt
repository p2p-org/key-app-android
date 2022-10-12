package org.p2p.wallet.solend.interactor

import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.repository.SolendDepositsRepository

private val COLLATERAL_ACCOUNTS = listOf("SOL", "USDT", "USDC", "BTC", "ETH")

class SolendDepositsInteractor(
    private val repository: SolendDepositsRepository
) {

    suspend fun getUserDeposits(tokenSymbols: List<String> = COLLATERAL_ACCOUNTS): List<SolendDepositToken> =
        repository.getUserDeposits(tokenSymbols)
}
