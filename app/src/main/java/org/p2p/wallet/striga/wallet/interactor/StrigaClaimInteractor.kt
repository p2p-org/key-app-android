package org.p2p.wallet.striga.wallet.interactor

import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isNotZero
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toFailureResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken
import org.p2p.wallet.striga.wallet.models.StrigaUserWalletAccount
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountCurrency
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.user.repository.UserLocalRepository

class StrigaClaimInteractor(
    private val strigaWalletRepository: StrigaWalletRepository,
    private val tokensRepository: UserLocalRepository,
    private val strigaFeatureToggle: StrigaSignupEnabledFeatureToggle
) {
    suspend fun getClaimableTokens(): StrigaDataLayerResult<List<StrigaClaimableToken>> {
        if (strigaFeatureToggle.isFeatureEnabled) {
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
                    claimableAmount = tokenAccount.availableBalance.fromLamports(tokenMetadata.decimals),
                    tokenDetails = tokenMetadata
                )
            }
            .toSuccessResult()
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
