package org.p2p.wallet.striga.wallet.interactor

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.common.model.map
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitEurOffRampDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWhitelistAddressesRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository

class StrigaNoBankingDetailsProvided : Throwable("No bic or iban found")

class StrigaWalletInteractor(
    private val walletRepository: StrigaWalletRepository,
    private val withdrawalsRepository: StrigaWithdrawalsRepository,
    private val whitelistAddressesRepository: StrigaWhitelistAddressesRepository
) {

    private class StrigaEuroAccountNotFound : Throwable()
    private class StrigaUsdcAccountNotFound : Throwable()

    suspend fun loadDetailsForStrigaAccounts(ignoreCache: Boolean = false): Result<Unit> = kotlin.runCatching {
        // 1. loading user wallet
        walletRepository.getUserWallet(ignoreCache)
        // 2. use preloaded wallet data in the following methods
        getFiatAccountDetails()
        getCryptoAccountDetails()
        Unit
    }.onFailure {
        Timber.e(it, "Unable to load striga accounts (fiat and crypto) details")
    }

    @Throws(StrigaEuroAccountNotFound::class, StrigaDataLayerError::class, Throwable::class)
    suspend fun getFiatAccountDetails(force: Boolean = false): StrigaFiatAccountDetails {
        val eurAccountId = walletRepository.getUserWallet(force)
            .map { it.eurAccount?.accountId }
            .successOrNull()
            ?: throw StrigaEuroAccountNotFound()
        return walletRepository.getFiatAccountDetails(eurAccountId).unwrap()
    }

    @Throws(StrigaEuroAccountNotFound::class)
    suspend fun getEurAccountId(): StrigaAccountId {
        return walletRepository.getUserWallet()
            .map { it.eurAccount?.accountId }
            .successOrNull()
            ?: throw StrigaEuroAccountNotFound()
    }

    @Throws(StrigaUsdcAccountNotFound::class, StrigaDataLayerError::class, Throwable::class)
    suspend fun getCryptoAccountDetails(force: Boolean = false): StrigaCryptoAccountDetails {
        val usdcAccountId = walletRepository.getUserWallet(force)
            .map { it.usdcAccount?.accountId }
            .successOrNull()
            ?: throw StrigaUsdcAccountNotFound()
        return walletRepository.getCryptoAccountDetails(usdcAccountId).unwrap()
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

    suspend fun initEurOffRamp(eurAmount: BigDecimal): StrigaInitEurOffRampDetails {
        val userBankingDetails = getEurBankingDetails()
        if (userBankingDetails.bankingIban.isNullOrEmpty() || userBankingDetails.bankingBic.isNullOrEmpty()) {
            throw StrigaNoBankingDetailsProvided()
        }

        return withdrawalsRepository.initEurOffRamp(
            sourceAccountId = getEurAccountId(),
            amount = eurAmount,
            iban = userBankingDetails.bankingIban,
            bic = userBankingDetails.bankingBic
        ).unwrap()
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

    suspend fun getEurBankingDetails(): StrigaUserBankingDetails {
        return walletRepository.getUserBankingDetails(getEurAccountId()).unwrap()
    }

    suspend fun saveNewEurBankingDetails(userBic: String, userIban: String) {
        walletRepository.saveUserEurBankingDetails(userBic, userIban)
    }
}
