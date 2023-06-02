package org.p2p.wallet.striga.onboarding

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
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

    override fun onCurrentCountryChanged(selectedCountry: Country) {
        launch {
            interactor.saveCurrentCountry(selectedCountry)

            view?.setCurrentCountry(selectedCountry)
            if (isCountrySupported(selectedCountry)) {
                view?.setAvailabilityState(StrigaOnboardingContract.View.AvailabilityState.Available)
            } else {
                view?.setAvailabilityState(StrigaOnboardingContract.View.AvailabilityState.Unavailable)
            }
        }
    }

    override fun onClickContinue() {
        // todo: find where we should navigate next
        view?.navigateNext()
    }

    override fun onClickHelp() {
        view?.openHelp()
    }

    private fun isCountrySupported(country: Country): Boolean {
        return interactor.checkIsCountrySupported(country)
    }
}
