package org.p2p.wallet.striga.onboarding

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.repository.StrigaPresetDataLocalRepository

class StrigaOnboardingPresenter(
    dispatchers: CoroutineDispatchers,
    private val countryRepository: CountryRepository,
    private val strigaPresetDataLocalRepository: StrigaPresetDataLocalRepository
) : BasePresenter<StrigaOnboardingContract.View>(dispatchers.ui), StrigaOnboardingContract.Presenter {

    override fun attach(view: StrigaOnboardingContract.View) {
        super.attach(view)

        launch {
            onCountrySelected(countryRepository.detectCountryOrDefault())
        }
    }

    override fun onCountrySelected(country: Country) {
        view?.setCurrentCountry(country)
        if (isCountrySupported(country)) {
            view?.setButtonState(StrigaOnboardingContract.View.ButtonState.Continue)

            // todo: save country
        } else {
            view?.setButtonState(StrigaOnboardingContract.View.ButtonState.ChangeCountry)
        }
    }

    override fun onClickContinue() {
        // todo: find where we should navigate next
        view?.navigateNext()
    }

    override fun onClickChangeCountry() {
        view?.openCountrySelection()
    }

    private fun isCountrySupported(country: Country): Boolean {
        return strigaPresetDataLocalRepository.checkIsCountrySupported(country)
    }
}
