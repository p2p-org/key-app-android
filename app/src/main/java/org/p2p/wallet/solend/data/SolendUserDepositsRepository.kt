package org.p2p.wallet.solend.data

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.sdk.facade.model.SolendCollateralAccountResponse
import org.p2p.wallet.sdk.facade.model.SolendPool
import org.p2p.wallet.sdk.facade.model.SolendUserDepositResponse
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

class SolendUserDepositsRepository(
    private val solanaFacade: SolendSdkFacade,
    private val ownerAddressProvider: TokenKeyProvider,
    private val currentSolendPool: SolendPool
) {
    private val ownerAddress: Base58String
        get() = ownerAddressProvider.publicKey.toBase58Instance()

    suspend fun getCollateralAccounts(): List<SolendCollateralAccountResponse> {
        return solanaFacade.getSolendCollateralAccounts(ownerAddress)
    }

    suspend fun getDeposits(): List<SolendUserDepositResponse> {
        return solanaFacade.getAllSolendUserDeposits(ownerAddress, currentSolendPool)
    }
}
