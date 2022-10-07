package org.p2p.wallet.solend.repository

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.sdk.facade.model.SolendPool
import org.p2p.wallet.solend.model.SolendDepositMapper
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendMarketInfo
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

class SolendDepositsRemoteRepository(
    private val solanaFacade: SolendSdkFacade,
    private val ownerAddressProvider: TokenKeyProvider,
    private val mapper: SolendDepositMapper,
    private val userLocalRepository: UserLocalRepository
) : SolendDepositsRepository {

    private val currentSolendPool = SolendPool.MAIN

    private val ownerAddress: Base58String
        get() = ownerAddressProvider.publicKey.toBase58Instance()

    override suspend fun getUserDeposits(tokenSymbols: List<String>): List<SolendDepositToken> {
        val marketsInfo = try {
            getSolendMarketInfo(tokenSymbols)
        } catch (e: Throwable) {
            Timber.e(e, "Error loading user marketsInfo")
            emptyList()
        }

        val deposits = try {
            val response = solanaFacade.getAllSolendUserDeposits(ownerAddress, currentSolendPool)
            response.map { mapper.fromNetwork(it) }
        } catch (e: Throwable) {
            Timber.e(e, "Error loading user deposits")
            emptyList()
        }

        val noMarketInfoButHasDeposits = marketsInfo.isEmpty() && deposits.isNotEmpty()
        return if (noMarketInfoButHasDeposits) {
            deposits.mapNotNull { deposit ->
                val tokenData =
                    userLocalRepository.findTokenDataBySymbol(deposit.depositTokenSymbol) ?: return@mapNotNull null
                val tokenPrice =
                    userLocalRepository.getPriceByToken(deposit.depositTokenSymbol) ?: return@mapNotNull null
                mapper.fromNetwork(
                    tokenData = tokenData,
                    tokenPrice = tokenPrice,
                    marketInfo = null,
                    activeDeposit = deposit
                )
            }
        } else {
            marketsInfo.mapNotNull { info ->
                val tokenData = userLocalRepository.findTokenDataBySymbol(info.tokenSymbol) ?: return@mapNotNull null
                val tokenPrice = userLocalRepository.getPriceByToken(info.tokenSymbol) ?: return@mapNotNull null
                val activeDeposit = deposits.find { it.depositTokenSymbol == info.tokenSymbol }
                mapper.fromNetwork(
                    tokenData = tokenData,
                    tokenPrice = tokenPrice,
                    marketInfo = info,
                    activeDeposit = activeDeposit
                )
            }
        }
    }

    private suspend fun getSolendMarketInfo(tokens: List<String>): List<SolendMarketInfo> {
        val response = solanaFacade.getSolendMarketInfo(tokens, currentSolendPool.poolName)
        return mapper.fromNetwork(response)
    }
}
