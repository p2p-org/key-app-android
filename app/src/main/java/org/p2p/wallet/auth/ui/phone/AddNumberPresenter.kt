package org.p2p.wallet.auth.ui.phone

import android.content.Context
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter

class AddNumberPresenter(
    private val countryCodeInteractor: CountryCodeInteractor,
    private val context: Context
) : BasePresenter<AddNumberContract.View>(), AddNumberContract.Presenter {

    private var selectedCountry: CountryCode? = null
    override fun load() {
        val countryCode: CountryCode? =
            countryCodeInteractor.detectSimCountry(context)
                ?: countryCodeInteractor.detectNetworkCountry(context)
                ?: countryCodeInteractor.detectLocaleCountry(context.resources)

        selectedCountry = countryCode

        view?.showDefaultCountryCode(countryCode)
    }

    override fun onCountryCodeChanged(newCountryCode: String) {
        val countryCode = countryCodeInteractor.findCountryForPhoneCode(newCountryCode)
        if (countryCode != null) {
            selectedCountry = countryCode
            view?.update(countryCode)
        } else {
            view?.showNoCountry()
        }
    }

    override fun onPhoneChanged(phoneNumber: String) {
        val isValidNumber = countryCodeInteractor.isValidNumberForRegion(selectedCountry!!.nameCode, phoneNumber)
        view?.showEnabled(isValidNumber)
    }

    override fun onCountryChanged(newCountry: CountryCode) {
        selectedCountry = newCountry
        view?.update(newCountry)
    }

    override fun onCountryClicked() {
        view?.showCountryPicked(selectedCountry)
    }
}
