package org.p2p.wallet.striga.offramp.interactor

import java.math.BigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.orZero
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.common.model.toFailureResult
import org.p2p.wallet.striga.common.model.toSuccessResult
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate
import org.p2p.wallet.striga.offramp.interactor.polling.StrigaOffRampExchangeRateNotifier
import org.p2p.wallet.striga.offramp.models.StrigaOffRampRateState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampToken
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenType
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.models.StrigaUserWalletAccount
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountCurrency
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.utils.divideSafe

class StrigaOffRampInteractor(
    private val exchangeRateNotifier: StrigaOffRampExchangeRateNotifier,
    private val strigaWalletRepository: StrigaWalletRepository,
    private val strigaFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val strigaUserInteractor: StrigaUserInteractor,
) {
    private val isOffRampDisabled: Boolean
        get() = !strigaFeatureToggle.isFeatureEnabled || !strigaUserInteractor.isKycApproved

    fun observeExchangeRateState(): StateFlow<StrigaOffRampRateState> = exchangeRateNotifier.observeExchangeRateState()

    fun startExchangeRateNotifier(scope: CoroutineScope) {
        exchangeRateNotifier.start(scope)
    }

    fun stopExchangeRateNotifier() {
        exchangeRateNotifier.stop()
    }

    fun calculateAmountByRate(
        tokenType: StrigaOffRampTokenType,
        rate: StrigaExchangeRate?,
        amount: BigDecimal
    ): BigDecimal {
        return when (tokenType) {
            StrigaOffRampTokenType.TokenA -> {
                amount.divideSafe(rate?.sellRate.orZero())
            }
            StrigaOffRampTokenType.TokenB -> {
                amount * rate?.sellRate.orZero()
            }
        }
    }

    suspend fun getOffRampTokens(): StrigaDataLayerResult<List<StrigaOffRampToken>> {
        if (isOffRampDisabled) {
            return emptyList<StrigaOffRampToken>().toSuccessResult()
        }

        val getWalletResult = strigaWalletRepository.getUserWallet()
        if (getWalletResult is StrigaDataLayerResult.Failure) {
            return getWalletResult.error.toFailureResult()
        }
        val userWallet = getWalletResult.unwrap()

        return userWallet.accounts
            .filter(::isTokenSuitsForOffRamp)
            .map { tokenAccount ->
                StrigaOffRampToken(
                    totalAmount = tokenAccount.availableBalanceLamports,
                    walletId = userWallet.walletId,
                    accountId = tokenAccount.accountId,
                )
            }
            .toSuccessResult()
    }

    private fun isTokenSuitsForOffRamp(tokenAccount: StrigaUserWalletAccount): Boolean {
        return tokenAccount.run {
            accountCurrency == StrigaWalletAccountCurrency.EUR && availableBalance.isNotZero()
        }
    }
}
