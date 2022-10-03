package org.p2p.wallet.solend.interactor

import org.p2p.wallet.solend.repository.SolendDepositsRepository
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendDomainMapper
import org.p2p.wallet.user.repository.UserLocalRepository

class SolendDepositsInteractor(
    private val repository: SolendDepositsRepository,
    private val userLocalRepository: UserLocalRepository,
    private val mapper: SolendDomainMapper
) {

    suspend fun getAllDeposits(tokenSymbols: List<String>): List<SolendDepositToken> {
        val marketsInfo = repository.getSolendMarketInfo(tokenSymbols)
        val activeDeposits = repository.getDeposits()

        return marketsInfo.mapNotNull { info ->
            val tokenData = userLocalRepository.findTokenDataBySymbol(info.tokenSymbol) ?: return@mapNotNull null
            val activeDeposit = activeDeposits.find { it.depositTokenSymbol == info.tokenSymbol }
            mapper.toDepositToken(tokenData, info, activeDeposit)
        }
    }
}
