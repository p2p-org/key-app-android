package org.p2p.wallet.home.addmoney.ui

import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.addmoney.AddMoneyContract
import org.p2p.wallet.home.addmoney.interactor.AddMoneyInteractor
import org.p2p.wallet.home.addmoney.mapper.AddMoneyUiMapper
import org.p2p.wallet.home.addmoney.model.AddMoneyButton
import org.p2p.wallet.home.addmoney.model.AddMoneyButtonType
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.striga.user.interactor.StrigaSignupDataEnsurerInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.user.interactor.UserInteractor

private const val TAG = "AddMoneyPresenter"

class AddMoneyPresenter(
    private val appFeatureFlags: InAppFeatureFlags,
    private val interactor: AddMoneyInteractor,
    private val userInteractor: UserInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val strigaSignupDataEnsurerInteractor: StrigaSignupDataEnsurerInteractor,
    private val addMoneyMapper: AddMoneyUiMapper,
) : BasePresenter<AddMoneyContract.View>(),
    AddMoneyContract.Presenter {

    private val bankTransferProgress = MutableStateFlow(false)

    override fun onButtonClick(button: AddMoneyButton) {
        when (button.type) {
            AddMoneyButtonType.BANK_TRANSFER_MOONPAY -> onBankTransferMoonpayClicked()
            AddMoneyButtonType.BANK_TRANSFER_STRIGA -> onBankTransferStrigaClicked()
            AddMoneyButtonType.BANK_CARD -> onBankCardClicked()
            AddMoneyButtonType.CRYPTO -> onCryptoClicked()
        }
    }

    override fun attach(view: AddMoneyContract.View) {
        super.attach(view)
        launch {
            try {
                // There's a tiny issue: buttons might take a noticeable delay to load
                // when the view is already displayed, leaving the user with an empty view during that time
                // and we don't have a progress bar to indicate the loading process
                // This situation will occur only if user is logged in without internet access/github error
                // and couldn't load the new country list beforehand
                val buttons = interactor.getAddMoneyButtons().map(addMoneyMapper::mapToCellItem)
                view.setCellItems(buttons)
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Unable to load add money buttons")
                view.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }

        bankTransferProgress
            .onEach { isLoading ->
                try {
                    val buttons = interactor.getAddMoneyButtons()
                        .map {
                            addMoneyMapper.mapButtonIsLoading(
                                button = it,
                                ifType = AddMoneyButtonType.BANK_TRANSFER_STRIGA,
                                isLoading = isLoading
                            )
                        }

                    view.setCellItems(buttons)
                } catch (e: Throwable) {
                    Timber.tag(TAG).e(e, "Unable to load add money buttons")
                    view.showUiKitSnackBar(messageResId = R.string.error_general_message)
                }
            }
            .launchIn(this)
    }

    private fun loadButtons() {
    }

    private fun onBankTransferMoonpayClicked() {
        launch {
            val tokenToBuy = userInteractor.getSingleTokenForBuy()
            tokenToBuy?.let {
                view?.navigateToBankCard(it, PaymentMethod.MethodType.BANK_TRANSFER)
            }
        }
    }

    private fun onBankTransferStrigaClicked() {
        // in case of simulation web3 user, we don't need to check metadata
        if (appFeatureFlags.strigaSimulateWeb3Flag.featureValue) {
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
