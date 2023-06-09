package org.p2p.wallet.striga.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
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
    private val strigaUserStatus = MutableSharedFlow<StrigaUserStatus>(replay = 1)

    init {
        launch {
            strigaBankTransferProgress.emit(true)
            strigaUserStatus.emit(
                strigaUserInteractor.getUserStatus()
            )
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
            //
            strigaBankTransferProgress.filter { it }
                .onEach {
                    view.showStrigaBankTransferView(
                        navigationTarget = BankTransferNavigationTarget.Nowhere,
                        showProgress = true
                    )
                }
                .launchIn(this)

            strigaUserStatus
                .onEach(::handleStrigaUserStatus)
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

    private fun handleStrigaUserStatus(userStatus: StrigaUserStatus) {
        when {
            !userStatus.isUserCreated -> {
                view?.showStrigaBankTransferView(
                    navigationTarget = BankTransferNavigationTarget.StrigaOnboarding,
                    showProgress = false
                )
            }
            !userStatus.isMobileVerified -> {
                view?.showStrigaBankTransferView(
                    navigationTarget = BankTransferNavigationTarget.StrigaSmsVerification,
                    showProgress = false
                )
            }
            userStatus.kycStatus == StrigaUserVerificationStatus.INITIATED -> {
                view?.showStrigaBankTransferView(
                    navigationTarget = BankTransferNavigationTarget.SumSubVerification,
                    showProgress = false
                )
            }
            else -> {
                // other statuses don't imply doing something, so should we just hide the view?
                view?.showStrigaBankTransferView(
                    navigationTarget = BankTransferNavigationTarget.Nowhere,
                    showProgress = false
                )
            }
        }
    }
}
