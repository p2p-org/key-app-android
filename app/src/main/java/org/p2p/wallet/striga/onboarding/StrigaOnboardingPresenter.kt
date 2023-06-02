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

    override fun attach(view: StrigaOnboardingContract.View) {
        super.attach(view)

        launch {
            onCountrySelected(interactor.getChosenCountry())
        }
    }

    private fun onCountrySelected(country: Country) {
        view?.setCurrentCountry(country)
        if (isCountrySupported(country)) {
            view?.setAvailabilityState(StrigaOnboardingContract.View.AvailabilityState.Available)

            // todo: save country
            // interactor.saveUserCountry(country)
        } else {
            view?.setAvailabilityState(StrigaOnboardingContract.View.AvailabilityState.Unavailable)
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
