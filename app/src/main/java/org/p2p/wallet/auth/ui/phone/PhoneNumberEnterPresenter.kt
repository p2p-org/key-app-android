package org.p2p.wallet.auth.ui.phone

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

class PhoneNumberEnterPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val createWalletInteractor: CreateWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor
) : BasePresenter<PhoneNumberEnterContract.View>(), PhoneNumberEnterContract.Presenter {

    private var selectedCountryCode: CountryCode? = null
    private var lastFullPhoneNumber: String = emptyString()

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
        } catch (e: Exception) {
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
            try {
                selectedCountryCode?.let {
                    val fullUserPhoneNumber = it.phoneCode + phoneNumber
                    if (lastFullPhoneNumber != fullUserPhoneNumber) {
                        createWalletInteractor.startCreatingWallet(userPhoneNumber = fullUserPhoneNumber)
                        lastFullPhoneNumber = fullUserPhoneNumber
                    }
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
}
