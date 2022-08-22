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
        launch {
            try {
                val countryCode: CountryCode? =
                    countryCodeInteractor.detectCountryCodeBySimCard()
                        ?: countryCodeInteractor.detectCountryCodeByNetwork()
                        ?: countryCodeInteractor.detectCountryCodeByLocale()

                selectedCountryCode = countryCode

                view?.showDefaultCountryCode(countryCode)
            } catch (e: Exception) {
                Timber.i(e)
                view?.showErrorMessage(e)
            }
        }
    }

    override fun onCountryCodeChanged(newCountryCode: String) {
        launch {
            val countryCode = countryCodeInteractor.findCountryForPhoneCode(newCountryCode)
            selectedCountryCode = countryCode
            view?.update(countryCode)
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
        view?.update(newCountry)
    }

    override fun onCountryCodeInputClicked() {
        view?.showCountryCodePicker(selectedCountryCode)
    }

    override fun submitUserPhoneNumber(phoneNumber: String) {
        launch {
            try {
                selectedCountryCode?.let {
                    createWalletInteractor.startCreatingWallet(userPhoneNumber = it.phoneCode + phoneNumber)
                }
                view?.navigateToSmsInput()
            } catch (smsDeliverFailed: GatewayServiceError.SmsDeliverFailed) {
                Timber.i(smsDeliverFailed)
                view?.showSmsDeliveryFailedForNumber()
            } catch (tooManyPhoneEnters: GatewayServiceError.TooManyRequests) {
                Timber.i(tooManyPhoneEnters)
                view?.navigateToAccountBlocked()
            } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
                Timber.i(serverError)
                view?.navigateToCriticalErrorScreen(serverError.code)
            } catch (error: Throwable) {
                Timber.e(error)
                view?.showErrorSnackBar(R.string.error_general_message)
            }
        }
    }
}
