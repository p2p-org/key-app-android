package org.p2p.wallet.home.addmoney.ui

import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.addmoney.AddMoneyDialogContract
import org.p2p.wallet.home.addmoney.interactor.AddMoneyDialogInteractor
import org.p2p.wallet.home.addmoney.model.AddMoneyItemType
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.striga.user.interactor.StrigaSignupDataEnsurerInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.user.interactor.UserInteractor

class AddMoneyDialogPresenter(
    private val appFeatureFlags: InAppFeatureFlags,
    private val interactor: AddMoneyDialogInteractor,
    private val userInteractor: UserInteractor,
    private val strigaSignupFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val strigaSignupDataEnsurerInteractor: StrigaSignupDataEnsurerInteractor,
) : BasePresenter<AddMoneyDialogContract.View>(),
    AddMoneyDialogContract.Presenter {

    private val isUserAuthByWeb3: Boolean
        get() = seedPhraseProvider.isWeb3AuthUser || appFeatureFlags.strigaSimulateWeb3Flag.featureValue

    private val isStrigaEnabled: Boolean
        get() = strigaSignupFeatureToggle.isFeatureEnabled && isUserAuthByWeb3

    private val bankTransferProgress = MutableStateFlow(false)

    override fun onItemClick(itemType: AddMoneyItemType) {
        when (itemType) {
            AddMoneyItemType.BankTransfer -> onBankTransferClicked()
            AddMoneyItemType.BankCard -> onBankCardClicked()
            AddMoneyItemType.Crypto -> onCryptoClicked()
        }
    }

    override fun attach(view: AddMoneyDialogContract.View) {
        super.attach(view)
        view.setCellItems(interactor.getAddMoneyCells())

        bankTransferProgress
            .onEach { view.showItemProgress(AddMoneyItemType.BankTransfer, it) }
            .launchIn(this)
    }

    private fun onBankTransferClicked() {
        if (!isStrigaEnabled) {
            launch {
                val tokenToBuy = userInteractor.getSingleTokenForBuy()
                tokenToBuy?.let {
                    view?.navigateToBankCard(it, PaymentMethod.MethodType.BANK_TRANSFER)
                }
            }
            return
        }
        // in case of simulation web3 user, we don't need to check metadata
        if (appFeatureFlags.strigaSimulateWeb3Flag.featureValue) {
            view?.navigateToBankTransferTarget(StrigaUserStatusDestination.SIGNUP_FORM)
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

    private fun onBankCardClicked() {
        launch {
            try {
                val tokenToBuy = userInteractor.getSingleTokenForBuy() ?: error("No token to buy")
                view?.navigateToBankCard(tokenToBuy, PaymentMethod.MethodType.CARD)
            } catch (e: Throwable) {
                Timber.e(e, "Failed to get token to buy")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun onCryptoClicked() {
        view?.navigateToCrypto()
    }
}
