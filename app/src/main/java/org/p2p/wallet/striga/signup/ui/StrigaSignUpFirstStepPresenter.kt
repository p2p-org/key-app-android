package org.p2p.wallet.striga.signup.ui

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpFirstStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor
) :
    BasePresenter<StrigaSignUpFirstStepContract.View>(dispatchers.ui),
    StrigaSignUpFirstStepContract.Presenter {

    private var countryOfBirth: Country? = null

    override fun firstAttach() {
        super.firstAttach()
        launch {
            initialLoadSignupData()
            setupPhoneMask()
        }
    }

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) {
        interactor.notifyDataChanged(type, newValue)
        // enabling button if something changed
        view?.setButtonIsEnabled(true)
    }

    override fun onCountryChanged(newCountry: Country) {
        countryOfBirth = newCountry
        view?.updateSignupField(
            newValue = "${newCountry.flagEmoji} ${newCountry.name}",
            type = StrigaSignupDataType.COUNTRY_OF_BIRTH
        )
    }

    override fun onCountryClicked() {
        view?.showCountryPicker(selectedCountry = countryOfBirth)
    }

    override fun onStop() {
        interactor.saveChanges()
    }

    override fun onSubmit() {
        view?.clearErrors()

        val (isValid, states) = interactor.validateFirstStep()

        if (isValid) {
            view?.navigateNext()
        } else {
            Timber.d("Validation failed: $states")
            view?.setErrors(states)
            // disable button is there are errors
            view?.setButtonIsEnabled(false)
            states.firstOrNull { !it.isValid }?.let {
                view?.scrollToFirstError(it.type)
            }
        }
    }

    private suspend fun initialLoadSignupData() {
        val data = interactor.getSignupData().associateBy { it.type }
        data.values.forEach {
            interactor.notifyDataChanged(it.type, it.value.orEmpty())
            view?.updateSignupField(it.type, it.value.orEmpty())
        }

        // db stores COUNTRY_OF_BIRTH as country name code ISO 3166-1 alpha-3,
        // so we need to find country by code and convert to country name
        val savedCountry = interactor.findCountryByNameCode(
            data[StrigaSignupDataType.COUNTRY_OF_BIRTH]?.value.orEmpty()
        )
        savedCountry?.let { onCountryChanged(savedCountry) }
    }

    private suspend fun setupPhoneMask() {
        val country = Country(name = "Turkey", flagEmoji = "", code = "TR") // interactor.getSelectedCountry()
        val maskForFormatter = "+" + interactor.findPhoneMaskByCountry(country)
        view?.setPhoneMask(maskForFormatter)
    }
}
