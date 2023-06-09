package org.p2p.wallet.striga.ui

import timber.log.Timber
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.striga.signup.model.StrigaUserStatus
import org.p2p.wallet.striga.ui.TopUpWalletContract.BankTransferNavigationTarget
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus
import org.p2p.wallet.user.interactor.UserInteractor

class TopUpWalletPresenter(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val strigaSignupFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val strigaUserInteractor: StrigaUserInteractor,
) : BasePresenter<TopUpWalletContract.View>(),
    TopUpWalletContract.Presenter {

    private val strigaBankTransferProgress = MutableSharedFlow<Boolean>(replay = 1)
    private val strigaUserStatus = MutableStateFlow<StrigaUserStatus?>(null)

    init {
        launch {
            loadUserStatus()
        }
    }

    private val isUserAuthByWeb3: Boolean
        get() {
            return seedPhraseProvider.getUserSeedPhrase().provider == SeedPhraseSource.WEB_AUTH ||
                appFeatureFlags.strigaSimulateWeb3Flag.featureValue
        }

    override fun attach(view: TopUpWalletContract.View) {
        super.attach(view)

        if (strigaSignupFeatureToggle.isFeatureEnabled && isUserAuthByWeb3) {
            strigaBankTransferProgress.onEach(view::showStrigaBankTransferView)
                .launchIn(this)
        } else {
            view.hideStrigaBankTransferView()
        }

        launch {
            val tokenToBuy = userInteractor.getSingleTokenForBuy()
            tokenToBuy?.let(view::showBankCardView) ?: view.hideBankCardView()
        }

        view.showCryptoReceiveView()
    }

    override fun onBankTransferClicked() {
        if (strigaUserInteractor.isUserCreated() && strigaUserStatus.value == null) {
            Timber.d("Striga user status is not fetched. Trying again...")
            launch {
                loadUserStatus()
            }
        } else {
            val target = getBankTransferNavigationTarget(strigaUserStatus.value)
            if (target == BankTransferNavigationTarget.StrigaSmsVerification) {
                // todo: send sms or maybe we should send first sms directly from sms verification screen?
            }
            view?.navigateToBankTransferTarget(target)
        }
    }

    private suspend fun loadUserStatus() {
        if (!strigaUserInteractor.isUserCreated()) {
            return
        }

        val status = strigaUserInteractor.getSavedUserStatus()
        if (status != null) {
            strigaUserStatus.emit(status)
            strigaBankTransferProgress.emit(false)
        } else {
            strigaBankTransferProgress.emit(true)
            strigaUserStatus.emit(strigaUserInteractor.getUserStatus())
            strigaBankTransferProgress.emit(false)
        }
    }

    private fun getBankTransferNavigationTarget(userStatus: StrigaUserStatus?): BankTransferNavigationTarget {
        if (userStatus == null) return BankTransferNavigationTarget.Nowhere

        return when {
            !strigaUserInteractor.isUserCreated() -> {
                BankTransferNavigationTarget.StrigaOnboarding
            }
            !userStatus.isMobileVerified -> {
                BankTransferNavigationTarget.StrigaSmsVerification
            }
            userStatus.kycStatus == StrigaUserVerificationStatus.INITIATED -> {
                BankTransferNavigationTarget.SumSubVerification
            }
            else -> {
                // todo: on/off ramp
                BankTransferNavigationTarget.Nowhere
            }
        }
    }
}
