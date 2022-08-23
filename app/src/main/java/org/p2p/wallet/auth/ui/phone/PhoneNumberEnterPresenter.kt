package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch

class PhoneNumberEnterPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val createWalletInteractor: CreateWalletInteractor
) : BasePresenter<PhoneNumberEnterContract.View>(), PhoneNumberEnterContract.Presenter {

    private var selectedCountryCode: CountryCode? = null

    override fun load() {
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
            view?.showErrorSnackBar(R.string.error_general_message)
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
                    createWalletInteractor.startCreatingWallet(userPhoneNumber = it.phoneCode + phoneNumber)
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
                view?.showErrorSnackBar(R.string.error_general_message)
            }
        }
    }
}
