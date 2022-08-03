package org.p2p.wallet.auth.ui.phone

import android.content.Context
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter

class PhoneNumberEnterPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val context: Context
) : BasePresenter<PhoneNumberEnterContract.View>(), PhoneNumberEnterContract.Presenter {

    private var selectedCountryCode: CountryCode? = null
    override fun load() {
        launch {
            try {
                val countryCode: CountryCode? =
                    countryCodeInteractor.detectCountryCodeBySimCard(context)
                        ?: countryCodeInteractor.detectCountryCodeByNetwork(context)
                        ?: countryCodeInteractor.detectCountryCodeByLocale(context.resources)

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
            val isValidNumber = countryCodeInteractor.isValidNumberForRegion(it.nameCode, phoneNumber)
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
