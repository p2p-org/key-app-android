package org.p2p.wallet.auth.ui.phone

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter

class PhoneNumberEnterPresenter(
    private val countryCodeInteractor: CountryCodeInteractor
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
}
