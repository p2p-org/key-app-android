package org.p2p.wallet.striga.ui.firststep

import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpFirstStepPresenter :
    BasePresenter<StrigaSignUpFirstStepContract.View>(),
    StrigaSignUpFirstStepContract.Presenter {

    private var selectedCountry: Country? = null
    override fun onFieldChanged(newValue: String, tag: StrigaSignupDataType) = Unit

    override fun onCountryChanged(newCountry: Country) {
        selectedCountry = newCountry
        view?.updateSignupField(
            newValue = "${newCountry.flagEmoji} ${newCountry.name}",
            type = StrigaSignupDataType.COUNTRY
        )
    }

    override fun onCountryClicked() {
        view?.showCountryPicker(selectedCountry = selectedCountry)
    }
}
