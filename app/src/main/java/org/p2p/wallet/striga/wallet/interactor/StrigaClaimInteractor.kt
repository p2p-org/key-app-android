package org.p2p.wallet.striga.wallet.interactor

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
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.map
import org.p2p.wallet.striga.model.toFailureResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.user.repository.StrigaUserStatusRepository
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaUserWalletAccount
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountCurrency
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWhitelistAddressesRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository
import org.p2p.wallet.user.repository.UserLocalRepository

class StrigaClaimInteractor(
    private val dispatchers: CoroutineDispatchers,
    private val strigaWalletRepository: StrigaWalletRepository,
    private val strigaWithdrawalsRepository: StrigaWithdrawalsRepository,
    private val strigaWhitelistAddressesRepository: StrigaWhitelistAddressesRepository,
    private val tokensRepository: UserLocalRepository,
    private val strigaFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val userTokenKeyProvider: TokenKeyProvider,
    private val userStatusRepository: StrigaUserStatusRepository,
) {
    private val isClaimDisabled: Boolean
        get() {
            return !strigaFeatureToggle.isFeatureEnabled ||
                userStatusRepository.getUserVerificationStatus()?.isKycApproved != true
        }

    suspend fun getClaimableTokens(): StrigaDataLayerResult<List<StrigaClaimableToken>> {
        if (isClaimDisabled) {
            Timber.d("Striga user cannot get claimable tokens because of feature toggle or KYC status")
            return emptyList<StrigaClaimableToken>().toSuccessResult()
        }

        val getWalletResult = strigaWalletRepository.getUserWallet()
        if (getWalletResult is StrigaDataLayerResult.Failure) {
            return getWalletResult.error.toFailureResult()
        }
        val userWallet = getWalletResult.unwrap()

        return userWallet.accounts
            .filter(::isTokenClaimable)
            .mapNotNull { tokenAccount ->
                val tokenMetadata = getClaimableTokenMetadata(tokenAccount.accountCurrency.currencyName)
                    ?: return@mapNotNull null
                StrigaClaimableToken(
                    claimableAmount = tokenAccount.availableBalance,
                    tokenDetails = tokenMetadata,
                    walletId = userWallet.walletId,
                    accountId = tokenAccount.accountId
                )
            }
            .toSuccessResult()
    }

    suspend fun claim(
        amount: BigDecimal,
        token: StrigaClaimableToken
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

    private fun getClaimableTokenMetadata(tokenSymbol: String): Token? {
        return tokensRepository.findTokenDataBySymbol(tokenSymbol)
            ?.let { tokensRepository.findTokenByMint(it.mintAddress) }
    }

    private fun isTokenClaimable(tokenAccount: StrigaUserWalletAccount): Boolean {
        return tokenAccount.run {
            accountCurrency == StrigaWalletAccountCurrency.USDC && availableBalance.isNotZero()
        }
    }
}
