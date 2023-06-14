package org.p2p.wallet.striga.ui

import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.MetadataLoadStatus
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
    private val metadataInteractor: MetadataInteractor,
) : BasePresenter<TopUpWalletContract.View>(),
    TopUpWalletContract.Presenter {

    private val strigaBankTransferProgress = MutableStateFlow(false)
    private val strigaUserStatus = MutableStateFlow<StrigaUserStatus?>(null)

    init {
        launch {
            loadMetadataIfNot()
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
        when {

            // cannot fill the form, check whether user is created etc without metadata, loading if it's not loaded
            metadataInteractor.currentMetadata == null -> {
                Timber.i("Metadata is not fetched. Trying again...")
                launch {
                    loadMetadataIfNot()
                }
            }
            // checking again whether status is loaded, if it's not - loading ...
            strigaUserInteractor.isUserCreated() && strigaUserStatus.value == null -> {
                Timber.i("Striga user status is not fetched. Trying again...")
                launch {
                    loadUserStatus()
                }
            }
            else -> navigateToTarget()
        }
    }

    private suspend fun loadUserStatus() {
        if (!strigaUserInteractor.isUserCreated()) {
            return
        }

        val status = strigaUserInteractor.getSavedUserStatus()
        if (status != null) {
            strigaUserStatus.emit(status)
        } else {
            withProgress {
                strigaUserStatus.emit(strigaUserInteractor.getUserStatus())
            }
        }
    }

    private suspend fun loadMetadataIfNot() {
        if (metadataInteractor.currentMetadata != null) {
            return
        }

        withProgress {
            if (metadataInteractor.tryLoadAndSaveMetadata() is MetadataLoadStatus.Failure) {
                view?.showUiKitSnackBar(null, R.string.error_general_message)
            }
        }
    }

    private fun navigateToTarget() {
        val target = getBankTransferNavigationTarget(strigaUserStatus.value)

        Timber.d("Navigating to $target")
        if (target == BankTransferNavigationTarget.StrigaSmsVerification) {
            // todo: send sms or maybe we should send first sms directly from sms verification screen?
            launch {
                strigaUserInteractor.resendSmsForVerifyPhoneNumber()
                view?.navigateToBankTransferTarget(target)
            }
        } else {
            view?.navigateToBankTransferTarget(target)
        }
    }

    private fun getBankTransferNavigationTarget(userStatus: StrigaUserStatus?): BankTransferNavigationTarget {
        return when {
            !strigaUserInteractor.isUserCreated() && userStatus == null -> {
                BankTransferNavigationTarget.StrigaOnboarding
            }
            userStatus == null -> {
                Timber.e(
                    "Unable to navigate bank transfer since user status is null and user is created.\n" +
                        "Check that userStatus is fetched"
                )
                BankTransferNavigationTarget.Nowhere
            }
            !userStatus.isMobileVerified -> {
                BankTransferNavigationTarget.StrigaSmsVerification
            }
            userStatus.kycStatus == StrigaUserVerificationStatus.INITIATED -> {
                BankTransferNavigationTarget.SumSubVerification
            }
            else -> {
                BankTransferNavigationTarget.Nowhere // todo: on/off ramp
            }
        }
    }

    private suspend fun withProgress(block: suspend () -> Unit) {
        strigaBankTransferProgress.emit(true)
        block()
        strigaBankTransferProgress.emit(false)
    }
}
