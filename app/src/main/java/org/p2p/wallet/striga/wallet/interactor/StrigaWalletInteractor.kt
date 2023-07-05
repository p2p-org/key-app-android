package org.p2p.wallet.striga.wallet.interactor

import java.math.BigInteger
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.map
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWhitelistAddressesRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository

class StrigaWalletInteractor(
    private val walletRepository: StrigaWalletRepository,
    private val withdrawalsRepository: StrigaWithdrawalsRepository,
    private val whitelistAddressesRepository: StrigaWhitelistAddressesRepository
) {

    private class StrigaEuroAccountNotFound : Throwable()

    suspend fun loadFiatAccountAndUserWallet(): Result<StrigaFiatAccountDetails> {
        return kotlin.runCatching { getFiatAccountDetails() }
    }

    @Throws(Throwable::class)
    suspend fun getFiatAccountDetails(): StrigaFiatAccountDetails {
        val eurAccountId = walletRepository.getUserWallet()
            .map { it.eurAccount?.accountId }
            .unwrap()
            ?: throw StrigaEuroAccountNotFound()
        return walletRepository.getFiatAccountDetails(eurAccountId).unwrap()
    }

    /**
     * Possibly, this method should accept only amount, other fields should be taken from a local storage
     */
    suspend fun initWithdrawal(
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amount: BigInteger,
    ): StrigaDataLayerResult<StrigaInitWithdrawalDetails> {

        return withdrawalsRepository.initiateOnchainWithdrawal(
            sourceAccountId = sourceAccountId,
            whitelistedAddressId = whitelistedAddressId,
            amountInUnits = amount,
        )
    }

    suspend fun whitelistAddress(
        address: String,
        currency: StrigaNetworkCurrency,
        label: String?
    ): StrigaDataLayerResult<StrigaWhitelistedAddressItem> {
        return whitelistAddressesRepository.whitelistAddress(
            address = address,
            currency = currency,
            addressLabel = label,
        )
    }

    suspend fun getWhitelistedAddresses(): StrigaDataLayerResult<List<StrigaWhitelistedAddressItem>> {
        return whitelistAddressesRepository.getWhitelistedAddresses()
    }
}
