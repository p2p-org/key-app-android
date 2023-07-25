package org.p2p.wallet.striga.wallet.repository

import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId

interface StrigaWalletRepository {
    /**
     * Call enrichAccount to get deposit information
     * @param accountId The account id to get details for. !! Must be a fiat account (i.e. EUR) !!
     */
    suspend fun getFiatAccountDetails(
        accountId: StrigaAccountId
    ): StrigaDataLayerResult<StrigaFiatAccountDetails>

    /**
     * Call enrichAccount to get deposit information
     * @param accountId The account id to get details for. !! Must be a crypto account (i.e. BTC or USDC) !!
     */
    suspend fun getCryptoAccountDetails(
        accountId: StrigaAccountId
    ): StrigaDataLayerResult<StrigaCryptoAccountDetails>

    suspend fun getUserWallet(): StrigaDataLayerResult<StrigaUserWallet>

    suspend fun getUserBankingDetails(
        accountId: StrigaAccountId,
    ): StrigaDataLayerResult<StrigaUserBankingDetails>

    suspend fun saveUserEurBankingDetails(userBic: String, userIban: String)
}
