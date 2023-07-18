package org.p2p.wallet.striga.onramp.interactor

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.common.model.map
import org.p2p.wallet.striga.common.model.toFailureResult
import org.p2p.wallet.striga.common.model.toSuccessResult
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaOnchainWithdrawalFees
import org.p2p.wallet.striga.wallet.models.StrigaUserWalletAccount
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountCurrency
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWhitelistAddressesRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository
import org.p2p.wallet.user.repository.UserLocalRepository

class StrigaOnRampInteractor(
    private val dispatchers: CoroutineDispatchers,
    private val strigaWalletRepository: StrigaWalletRepository,
    private val strigaWithdrawalsRepository: StrigaWithdrawalsRepository,
    private val strigaWhitelistAddressesRepository: StrigaWhitelistAddressesRepository,
    private val tokensRepository: UserLocalRepository,
    private val strigaFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val userTokenKeyProvider: TokenKeyProvider,
    private val userInteractor: StrigaUserInteractor,
) {
    private val isOnRampDisabled: Boolean
        get() = !strigaFeatureToggle.isFeatureEnabled || !userInteractor.isKycApproved

    suspend fun getOnRampTokens(): StrigaDataLayerResult<List<StrigaOnRampToken>> {
        if (isOnRampDisabled) {
            Timber.d("Striga user cannot get onramp tokens because of feature toggle or KYC status")
            return emptyList<StrigaOnRampToken>().toSuccessResult()
        }

        val getWalletResult = strigaWalletRepository.getUserWallet()
        if (getWalletResult is StrigaDataLayerResult.Failure) {
            return getWalletResult.error.toFailureResult()
        }
        val userWallet = getWalletResult.unwrap()

        return userWallet.accounts
            .filter(::isTokenSuitsForOnRamp)
            .mapNotNull { tokenAccount ->
                val tokenMetadata = getClaimableTokenMetadata(tokenAccount) ?: return@mapNotNull null
                val fees = getFeesForOnRampToken(tokenAccount) ?: return@mapNotNull null
                StrigaOnRampToken(
                    totalAmount = tokenAccount.availableBalanceLamports,
                    tokenDetails = tokenMetadata,
                    walletId = userWallet.walletId,
                    accountId = tokenAccount.accountId,
                    fees = fees
                )
            }
            .toSuccessResult()
    }

    suspend fun onRampToken(
        amount: BigDecimal,
        token: StrigaOnRampToken
    ): StrigaDataLayerResult<StrigaWithdrawalChallengeId> = withContext(dispatchers.io) {
        try {
            val whitelistedAddressList = strigaWhitelistAddressesRepository
                .getWhitelistedAddresses()
                .unwrap()

            val whitelistedAddressId = whitelistedAddressList
                .firstOrNull { it.address == userTokenKeyProvider.publicKey }
                ?.id
                ?: whitelistUserAddress().unwrap()

            strigaWithdrawalsRepository.initiateOnchainWithdrawal(
                sourceAccountId = token.accountId,
                whitelistedAddressId = whitelistedAddressId,
                amountInUnits = amount.toLamports(STRIGA_FIAT_DECIMALS)
            ).map(StrigaInitWithdrawalDetails::challengeId)
        } catch (e: StrigaDataLayerError) {
            e.toFailureResult()
        } catch (e: Throwable) {
            StrigaDataLayerResult.Failure(StrigaDataLayerError.InternalError(e))
        }
    }

    private suspend fun whitelistUserAddress(): StrigaDataLayerResult<StrigaWhitelistedAddressId> {
        return strigaWhitelistAddressesRepository.whitelistAddress(
            address = userTokenKeyProvider.publicKey,
            currency = StrigaNetworkCurrency.USDC_SOL
        )
            .map { it.id }
    }

    private suspend fun getClaimableTokenMetadata(tokenAccount: StrigaUserWalletAccount): Token? {
        return tokensRepository.findTokenDataBySymbol(tokenAccount.accountCurrency.currencyName)
            ?.let { tokensRepository.findTokenByMint(it.mintAddress) }
    }

    private fun isTokenSuitsForOnRamp(tokenAccount: StrigaUserWalletAccount): Boolean {
        return tokenAccount.run {
            accountCurrency == StrigaWalletAccountCurrency.USDC && availableBalance.isNotZero()
        }
    }

    private suspend fun getFeesForOnRampToken(tokenAccount: StrigaUserWalletAccount): StrigaOnchainWithdrawalFees? {
        return strigaWithdrawalsRepository.getOnchainWithdrawalFees(
            sourceAccountId = tokenAccount.accountId,
            whitelistedAddressId = StrigaWhitelistedAddressId(userTokenKeyProvider.publicKey),
            amount = tokenAccount.availableBalanceLamports
        )
            .onFailure { Timber.e(it) }
            .successOrNull()
    }
}
