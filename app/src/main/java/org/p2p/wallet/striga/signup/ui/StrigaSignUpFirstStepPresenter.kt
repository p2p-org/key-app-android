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

    override fun attach(view: StrigaSignUpFirstStepContract.View) {
        super.attach(view)
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
        val data = interactor.getSignupData()
        data.forEach {
            interactor.notifyDataChanged(it.type, it.value.orEmpty())
            view?.updateSignupField(it.type, it.value.orEmpty())
        }
    }

    private suspend fun setupPhoneMask() {
        val country = Country(name = "Turkey", flagEmoji = "", code = "TR") // interactor.getSelectedCountry()
        val maskForFormatter = "+" + interactor.findPhoneMaskByCountry(country)
        view?.setPhoneMask(maskForFormatter)
    }
}
