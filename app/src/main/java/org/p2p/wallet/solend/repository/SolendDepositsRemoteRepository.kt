package org.p2p.wallet.solend.repository

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.sdk.facade.model.SolendPool
import org.p2p.wallet.solend.model.SolendDepositMapper
import org.p2p.wallet.solend.model.SolendMarketInfo
import org.p2p.wallet.solend.model.SolendUserDeposit
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber

class SolendDepositsRemoteRepository(
    private val solanaFacade: SolendSdkFacade,
    private val ownerAddressProvider: TokenKeyProvider,
    private val mapper: SolendDepositMapper
) : SolendDepositsRepository {

    private val currentSolendPool = SolendPool.MAIN

    private val ownerAddress: Base58String
        get() = ownerAddressProvider.publicKey.toBase58Instance()

    override suspend fun getDeposits(): List<SolendUserDeposit> = try {
        val response = solanaFacade.getAllSolendUserDeposits(ownerAddress, currentSolendPool)
        response.map { mapper.fromNetwork(it) }
    } catch (e: Throwable) {
        Timber.e(e, "Error loading user deposits")
        emptyList()
    }

    override suspend fun getSolendMarketInfo(tokens: List<String>): List<SolendMarketInfo> {
        val response = solanaFacade.getSolendMarketInfo(tokens, currentSolendPool.poolName)
        return mapper.fromNetwork(response)
    }
}
