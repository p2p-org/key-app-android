package org.p2p.wallet.striga.wallet.interactor

import java.math.BigInteger
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitiateOnchainWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository

class StrigaWalletInteractor(
    private val repository: StrigaWalletRepository,
    private val userIdProvider: StrigaUserIdProvider,
) {

    suspend fun getFiatAccountDetails(
        accountId: StrigaAccountId,
    ): StrigaDataLayerResult<StrigaFiatAccountDetails> {
        return repository.getFiatAccountDetails(userIdProvider.getUserIdOrThrow(), accountId)
    }

    /**
     * Possibly, this method should accept only amount, other fields should be taken from a local storage
     */
    suspend fun initOnchainWithdrawal(
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amount: BigInteger,
    ): StrigaDataLayerResult<StrigaInitiateOnchainWithdrawalDetails> {

        return repository.initiateOnchainWithdrawal(
            userId = userIdProvider.getUserIdOrThrow(),
            sourceAccountId = sourceAccountId,
            whitelistedAddressId = whitelistedAddressId,
            amount = amount,
        )
    }

    suspend fun addWhitelistedAddress(
        address: String,
        currency: StrigaNetworkCurrency,
        label: String?
    ): StrigaDataLayerResult<StrigaWhitelistedAddressItem> {
        return repository.addWhitelistedAddress(
            userId = userIdProvider.getUserIdOrThrow(),
            address = address,
            currency = currency,
            label = label,
        )
    }

    suspend fun getWhitelistedAddresses(): StrigaDataLayerResult<List<StrigaWhitelistedAddressItem>> {
        return repository.getWhitelistedAddresses(
            userId = userIdProvider.getUserIdOrThrow(),
        )
    }
}
