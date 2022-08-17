package org.p2p.wallet.auth.ui.phone

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class PhoneNumberEnterPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val createWalletInteractor: CreateWalletInteractor
) : BasePresenter<PhoneNumberEnterContract.View>(), PhoneNumberEnterContract.Presenter {

    private var selectedCountryCode: CountryCode? = null

    override fun attach(view: PhoneNumberEnterContract.View) {
        super.attach(view)
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
            view?.setContinueButtonEnabled(isValidNumber)
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
                    createWalletInteractor.startCreatingWallet(it.phoneCode + phoneNumber)
                }
                view?.navigateToSmsInput()
            } catch (gatewayError: GatewayServiceError) {
                // TODO PWN-4362 - add error handling
                Timber.i(gatewayError)
            } catch (createWalletError: CreateWalletInteractor.CreateWalletFailure) {
                Timber.i(createWalletError)
            } catch (error: Throwable) {
                Timber.i(error)
            }
        }
    }
}
