package org.p2p.wallet.home.ui.topup

import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.striga.user.interactor.StrigaSignupDataEnsurerInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.user.interactor.UserInteractor

class TopUpWalletPresenter(
    private val appFeatureFlags: InAppFeatureFlags,
    private val userInteractor: UserInteractor,
    private val strigaSignupFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val strigaSignupDataEnsurerInteractor: StrigaSignupDataEnsurerInteractor,
    private val inAppFeatureFlags: InAppFeatureFlags,
) : BasePresenter<TopUpWalletContract.View>(),
    TopUpWalletContract.Presenter {

    private val isUserAuthByWeb3: Boolean
        get() = seedPhraseProvider.isWeb3AuthUser || appFeatureFlags.strigaSimulateWeb3Flag.featureValue

    private val isStrigaEnabled: Boolean
        get() = strigaSignupFeatureToggle.isFeatureEnabled && isUserAuthByWeb3

    private val bankTransferProgress = MutableStateFlow(false)

    override fun attach(view: TopUpWalletContract.View) {
        super.attach(view)

        bankTransferProgress.onEach {
            view.showStrigaBankTransferView(showProgress = it, isStrigaEnabled = isStrigaEnabled)
        }
            .launchIn(this)

        launch {
            val tokenToBuy = userInteractor.getSingleTokenForBuy()
            tokenToBuy?.let(view::showBankCardView) ?: view.hideBankCardView()
        }

        view.showCryptoReceiveView()
    }

    override fun onBankTransferClicked() {
        if (!isStrigaEnabled) {
            launch {
                val tokenToBuy = userInteractor.getSingleTokenForBuy()
                tokenToBuy?.let {
                    view?.navigateToBuyWithTransfer(it)
                }
            }
            return
        }
        // in case of simulation web3 user, we don't need to check metadata
        if (inAppFeatureFlags.strigaSimulateWeb3Flag.featureValue) {
            view?.navigateToBankTransferTarget(StrigaUserStatusDestination.ONBOARDING)
            return
        }

        launch {
            try {
                bankTransferProgress.emit(true)

                strigaSignupDataEnsurerInteractor.ensureNeededDataLoaded()

                val strigaDestination = strigaUserInteractor.getUserDestination()

                when {
                    strigaDestination == StrigaUserStatusDestination.IBAN_ACCOUNT &&
                        strigaUserInteractor.isKycApproved -> {
                        // prefetch account details for IBAN
                        strigaWalletInteractor.getFiatAccountDetails()
                        // prefetch crypto account details for future use
                        strigaWalletInteractor.getCryptoAccountDetails()
                    }
                    strigaDestination == StrigaUserStatusDestination.KYC_PENDING -> {
                        view?.navigateToKycPending()
                        return@launch
                    }
                }

                view?.navigateToBankTransferTarget(strigaDestination)
            } catch (strigaDataLoadFailed: Throwable) {
                Timber.e(strigaDataLoadFailed, "failed to load needed data for bank transfer")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            } finally {
                bankTransferProgress.emit(false)
            }
        }
    }
}
