package org.p2p.wallet.striga.onboarding

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.onboarding.StrigaOnboardingContract.View.AvailabilityState
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor

class StrigaOnboardingPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaOnboardingInteractor,
) : BasePresenter<StrigaOnboardingContract.View>(dispatchers.ui), StrigaOnboardingContract.Presenter {

    private var selectedCountryCode: CountryCode? = null

    // to remove race between onCurrentCountryChanged and attach
    private val selectedCountryState = MutableStateFlow<CountryCode?>(null)

    override fun firstAttach() {
        super.firstAttach()
        launch {
            selectedCountryState.emit(interactor.getChosenCountry())
        }
    }

    override fun attach(view: StrigaOnboardingContract.View) {
        super.attach(view)
        selectedCountryState
            .filterNotNull()
            .onEach(::showCountry)
            .launchIn(this)
    }

    override fun onCurrentCountryChanged(selectedCountry: CountryCode) {
        launch {
            selectedCountryCode = selectedCountry
            interactor.saveCurrentCountry(selectedCountry)
            selectedCountryState.emit(selectedCountry)
        }
    }

    override fun onCountryClicked() {
        view?.showCountryPicker(selectedCountryCode)
    }

    private fun showCountry(country: CountryCode) {
        view?.setCurrentCountry(country)
        view?.setAvailabilityState(isCountrySupported(country))
    }

    override fun onClickContinue() {
        view?.navigateNext()
    }

    override fun onClickHelp() {
        view?.openHelp()
    }

    private fun isCountrySupported(country: CountryCode): AvailabilityState =
        if (interactor.checkIsCountrySupported(country)) {
            AvailabilityState.Available
        } else {
            AvailabilityState.Unavailable
        }
}
