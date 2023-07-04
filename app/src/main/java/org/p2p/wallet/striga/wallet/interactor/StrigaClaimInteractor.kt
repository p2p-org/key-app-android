package org.p2p.wallet.striga.wallet.interactor

import java.math.BigInteger
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isNotZero
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.map
import org.p2p.wallet.striga.model.toFailureResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaUserWalletAccount
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountCurrency
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.user.repository.UserLocalRepository

class StrigaClaimInteractor(
    private val dispatchers: CoroutineDispatchers,
    private val strigaWalletRepository: StrigaWalletRepository,
    private val tokensRepository: UserLocalRepository,
    private val strigaFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val userTokenKeyProvider: TokenKeyProvider
) {
    suspend fun getClaimableTokens(): StrigaDataLayerResult<List<StrigaClaimableToken>> {
        if (!strigaFeatureToggle.isFeatureEnabled) {
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
                    claimableAmount = tokenAccount.availableBalance.fromLamports(STRIGA_FIAT_DECIMALS),
                    tokenDetails = tokenMetadata,
                    walletId = userWallet.walletId,
                    accountId = tokenAccount.accountId
                )
            }
            .toSuccessResult()
    }

    suspend fun claim(
        amountInUnits: BigInteger,
        token: StrigaClaimableToken
    ): StrigaDataLayerResult<StrigaWithdrawalChallengeId> = withContext(dispatchers.io) {
        try {
            val whitelistedAddressList = strigaWalletRepository.getWhitelistedAddresses().unwrap()

            val whitelistedAddressId = whitelistedAddressList.firstOrNull {
                it.address == userTokenKeyProvider.publicKey
            }?.id ?: kotlin.run {
                strigaWalletRepository
                    .whitelistAddress(userTokenKeyProvider.publicKey, StrigaNetworkCurrency.USDC_SOL)
                    .map { it.id }
                    .unwrap()
            }

            strigaWalletRepository.initiateOnchainWithdrawal(
                sourceAccountId = token.accountId,
                whitelistedAddressId = whitelistedAddressId,
                amountInUnits = amountInUnits
            ).map { it.challengeId }
        } catch (e: StrigaDataLayerError) {
            e.toFailureResult()
        } catch (e: Throwable) {
            StrigaDataLayerResult.Failure(StrigaDataLayerError.InternalError(e))
        }
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
