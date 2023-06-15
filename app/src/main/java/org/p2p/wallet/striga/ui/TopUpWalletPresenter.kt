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
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.user.interactor.UserInteractor

class TopUpWalletPresenter(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val strigaSignupFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val metadataInteractor: MetadataInteractor,
    private val inAppFeatureFlags: InAppFeatureFlags,
) : BasePresenter<TopUpWalletContract.View>(),
    TopUpWalletContract.Presenter {

    private val isUserAuthByWeb3: Boolean
        get() {
            return seedPhraseProvider.getUserSeedPhrase().provider == SeedPhraseSource.WEB_AUTH ||
                appFeatureFlags.strigaSimulateWeb3Flag.featureValue
        }

    private val strigaBankTransferProgress = MutableStateFlow(false)

    init {
        launch {
            loadMetadataIfNot()
        }
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
        val userStatusNavigation = strigaUserInteractor.getUserDestination()
        if (userStatusNavigation == StrigaUserStatusDestination.SMS_VERIFICATION) {
            // todo: send sms or maybe we should send first sms directly from sms verification screen?
            launch {
                strigaUserInteractor.resendSmsForVerifyPhoneNumber()
            }
        }

        // in case of simulation web3 user, we don't need to check metadata
        if (inAppFeatureFlags.strigaSimulateWeb3Flag.featureValue) {
            view?.navigateToBankTransferTarget(userStatusNavigation)
            return;
        }

        when {
            // cannot fill the form, check whether user is created etc without metadata, loading if it's not loaded
            metadataInteractor.currentMetadata == null -> {
                Timber.i("Metadata is not fetched. Trying again...")
                launch {
                    loadMetadataIfNot()
                }
            }
            else -> {
                view?.navigateToBankTransferTarget(userStatusNavigation)
            }
        }
    }

    private suspend fun loadMetadataIfNot() {
        if (metadataInteractor.currentMetadata != null || inAppFeatureFlags.strigaSimulateWeb3Flag.featureValue) {
            return
        }

        withProgress {
            if (metadataInteractor.tryLoadAndSaveMetadata() is MetadataLoadStatus.Failure) {
                view?.showUiKitSnackBar(null, R.string.error_general_message)
            }
        }
    }

    private suspend fun withProgress(block: suspend () -> Unit) {
        strigaBankTransferProgress.emit(true)
        block()
        strigaBankTransferProgress.emit(false)
    }
}
