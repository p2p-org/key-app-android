package org.p2p.wallet.striga.onboarding

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

    override fun firstAttach() {
        super.firstAttach()
        launch {
            onCurrentCountryChanged(interactor.getChosenCountry())
        }
    }

    override fun onCurrentCountryChanged(selectedCountry: CountryCode) {
        launch {
            interactor.saveCurrentCountry(selectedCountry)

            view?.setCurrentCountry(selectedCountry)
            if (isCountrySupported(selectedCountry)) {
                view?.setAvailabilityState(AvailabilityState.Available)
            } else {
                view?.setAvailabilityState(AvailabilityState.Unavailable)
            }
        }
    }

    override fun onClickContinue() {
        view?.navigateNext()
    }

    override fun onClickHelp() {
        view?.openHelp()
    }

    private fun isCountrySupported(country: CountryCode): Boolean {
        return interactor.checkIsCountrySupported(country)
    }
}
