package org.p2p.wallet.home.onofframp.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.onofframp.OnOffRampCountrySelectionContract
import org.p2p.wallet.home.onofframp.interactor.OnOffRampCountrySelectionInteractor
import org.p2p.wallet.settings.interactor.SettingsInteractor

class OnOffRampCountrySelectionPresenter(
    dispatchers: CoroutineDispatchers,
    private val addMoneyOnboardingInteractor: OnOffRampCountrySelectionInteractor,
    private val settingsInteractor: SettingsInteractor,
) : BasePresenter<OnOffRampCountrySelectionContract.View>(dispatchers.ui), OnOffRampCountrySelectionContract.Presenter {

    private lateinit var selectedCountryCode: CountryCode

    // to remove race between onCurrentCountryChanged and attach
    private val selectedCountryState = MutableStateFlow<CountryCode?>(null)

    init {
        launch {
            selectedCountryCode = addMoneyOnboardingInteractor.getChosenCountry().also {
                selectedCountryState.emit(it)
            }
        }
    }

    override fun attach(view: OnOffRampCountrySelectionContract.View) {
        super.attach(view)
        selectedCountryState
            .filterNotNull()
            .onEach(::showCountry)
            .launchIn(this)
    }

    override fun onCurrentCountryChanged(selectedCountry: CountryCode) {
        launch {
            selectedCountryCode = selectedCountry
            settingsInteractor.userCountryCode = selectedCountry
            selectedCountryState.emit(selectedCountry)
        }
    }

    override fun onCountryClicked() {
        view?.showCountryPicker(settingsInteractor.userCountryCode)
    }

    override fun onNextClicked() {
        launch {
            addMoneyOnboardingInteractor.saveCurrentCountry(selectedCountryCode)
            view?.navigateNext()
        }
    }

    private fun showCountry(country: CountryCode) {
        view?.setCurrentCountry(country)
    }
}
