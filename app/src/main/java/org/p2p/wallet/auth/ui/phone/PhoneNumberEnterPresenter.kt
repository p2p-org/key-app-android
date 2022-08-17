package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.phone.PhoneNumberEnterContract.View.ContinueButtonState
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
            view?.setContinueButtonState(
                if (isValidNumber) {
                    ContinueButtonState.ENABLED_TO_CONTINUE
                } else {
                    ContinueButtonState.DISABLED_INPUT_IS_EMPTY
                }
            )
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
            } catch (tooManyPhoneEnters: GatewayServiceError.SmsDeliverFailed) {
                view?.showSmsDeliveryFailedForNumber()
            } catch (tooManyPhoneEnters: GatewayServiceError.TooManyRequests) {
                view?.navigateToAccountBlocked()
            } catch (gatewayError: GatewayServiceError) {
                view?.showErrorSnackBar(R.string.error_general_message)
            } catch (createWalletError: CreateWalletInteractor.CreateWalletFailure) {
                Timber.e(createWalletError)
                view?.showErrorSnackBar(R.string.error_general_message)
            } catch (error: Throwable) {
                Timber.e(error)
                view?.showErrorSnackBar(R.string.error_general_message)
            }
        }
    }
}
