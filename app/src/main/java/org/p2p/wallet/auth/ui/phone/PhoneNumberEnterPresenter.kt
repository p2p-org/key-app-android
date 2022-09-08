package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.CustomShareRestoreInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.model.PhoneNumber

private const val MAX_PHONE_NUMBER_TRIES = 5

class PhoneNumberEnterPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val createWalletInteractor: CreateWalletInteractor,
    private val customShareRestoreInteractor: CustomShareRestoreInteractor,
    private val onboardingInteractor: OnboardingInteractor
) : BasePresenter<PhoneNumberEnterContract.View>(), PhoneNumberEnterContract.Presenter {

    private var selectedCountryCode: CountryCode? = null
    private var lastFullPhoneNumber: String = emptyString()
    private var submitUserPhoneTriesCount = 0

    override fun attach(view: PhoneNumberEnterContract.View) {
        super.attach(view)

        when (onboardingInteractor.currentFlow) {
            OnboardingInteractor.OnboardingFlow.CREATE_WALLET -> view.initCreateWalletViews()
            OnboardingInteractor.OnboardingFlow.RESTORE_WALLET -> view.initRestoreWalletViews()
        }

        launch { loadDefaultCountryCode() }
    }

    private suspend fun loadDefaultCountryCode() {
        try {
            val countryCode: CountryCode? =
                countryCodeInteractor.detectCountryCodeBySimCard()
                    ?: countryCodeInteractor.detectCountryCodeByNetwork()
                    ?: countryCodeInteractor.detectCountryCodeByLocale()

            selectedCountryCode = countryCode

            view?.showDefaultCountryCode(countryCode)
        } catch (e: Throwable) {
            Timber.e(e, "Loading default country code failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun onCountryCodeChanged(newCountryCode: String) {
        launch {
            selectedCountryCode = countryCodeInteractor.findCountryForPhoneCode(newCountryCode)
            view?.update(selectedCountryCode)
        }
    }

    override fun onPhoneChanged(phoneNumber: String) {
        selectedCountryCode?.let {
            val isValidNumber = countryCodeInteractor.isValidNumberForRegion(it.phoneCode, phoneNumber)
            val newButtonState = if (isValidNumber) {
                PhoneNumberScreenContinueButtonState.ENABLED_TO_CONTINUE
            } else {
                PhoneNumberScreenContinueButtonState.DISABLED_INPUT_IS_EMPTY
            }
            view?.setContinueButtonState(newButtonState)
        }
    }

    override fun onCountryCodeChanged(newCountry: CountryCode) {
        selectedCountryCode = newCountry
        view?.update(selectedCountryCode)
    }

    override fun onCountryCodeInputClicked() {
        view?.showCountryCodePicker(selectedCountryCode)
    }

    override fun submitUserPhoneNumber(phoneNumber: String) {
        launch {
            when (onboardingInteractor.currentFlow) {
                OnboardingInteractor.OnboardingFlow.CREATE_WALLET -> startCreatingWallet(phoneNumber)
                OnboardingInteractor.OnboardingFlow.RESTORE_WALLET -> startRestoringCustomShare(phoneNumber)
            }
        }
    }

    private suspend fun startCreatingWallet(phoneNumber: String) {
        try {
            selectedCountryCode?.let {
                if (createWalletInteractor.getUserEnterPhoneNumberTriesCount() >= MAX_PHONE_NUMBER_TRIES) {
                    view?.navigateToAccountBlocked()
                } else {
                    val userPhoneNumber = PhoneNumber(it.phoneCode + phoneNumber)
                    createWalletInteractor.startCreatingWallet(userPhoneNumber = userPhoneNumber)
                    view?.navigateToSmsInput()
                }
            }
        } catch (smsDeliverFailed: GatewayServiceError.SmsDeliverFailed) {
            Timber.i(smsDeliverFailed)
            view?.showSmsDeliveryFailedForNumber()
        } catch (tooManyPhoneEnters: GatewayServiceError.TooManyRequests) {
            Timber.i(tooManyPhoneEnters)
            view?.navigateToAccountBlocked()
        } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
            Timber.e(serverError, "Phone number submission failed with critical error")
            view?.navigateToCriticalErrorScreen(serverError.code)
        } catch (error: Throwable) {
            Timber.e(error, "Phone number submission failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    private suspend fun startRestoringCustomShare(phoneNumber: String) {
        try {
            selectedCountryCode?.let {
                val userPhoneNumber = PhoneNumber(it.phoneCode + phoneNumber)
                customShareRestoreInteractor.startRestoreCustomShare(userPhoneNumber = userPhoneNumber)
                view?.navigateToSmsInput()
            }
        } catch (smsDeliverFailed: GatewayServiceError.SmsDeliverFailed) {
            Timber.i(smsDeliverFailed)
            view?.showSmsDeliveryFailedForNumber()
        } catch (tooManyPhoneEnters: GatewayServiceError.TooManyRequests) {
            Timber.i(tooManyPhoneEnters)
            view?.navigateToAccountBlocked()
        } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
            Timber.e(serverError, "Phone number submission failed with critical error")
            view?.navigateToCriticalErrorScreen(serverError.code)
        } catch (error: Throwable) {
            Timber.e(error, "Phone number submission failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }
}
