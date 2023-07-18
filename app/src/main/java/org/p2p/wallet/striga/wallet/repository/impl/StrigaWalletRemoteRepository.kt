package org.p2p.wallet.striga.wallet.repository.impl

import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletsResponse
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaAccountStatementMapper
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWalletMapper

internal class StrigaWalletRemoteRepository(
    private val api: StrigaWalletApi,
    private val walletMapper: StrigaWalletMapper,
    private val accountStatementMapper: StrigaAccountStatementMapper,
) {

    suspend fun getFiatAccountDetails(accountId: StrigaAccountId): StrigaFiatAccountDetails {
        val request = walletMapper.toNetworkEnrichAccount(accountId)
        val response = api.enrichFiatAccount(request)
        return walletMapper.fromNetwork(response)
    }

    suspend fun getCryptoAccountDetails(accountId: StrigaAccountId): StrigaCryptoAccountDetails {
        val request = walletMapper.toNetworkEnrichAccount(accountId)
        val response = api.enrichCryptoAccount(request)
        return walletMapper.fromNetwork(response)
    }

    suspend fun getUserWallet(): StrigaUserWallet {
        val request = walletMapper.toNetworkUserWallet()
        val response: StrigaUserWalletsResponse = api.getUserWallets(request)
        return walletMapper.fromNetwork(response)
    }

    suspend fun getAccountStatement(
        accountId: StrigaAccountId,
        strigaUserFullName: String
    ): StrigaUserBankingDetails {
        val request = accountStatementMapper.toNetworkAccountStatement(accountId)
        val response = api.getAccountStatement(request)
        return accountStatementMapper.fromNetwork(response, strigaUserFullName)
    }
}
