package org.p2p.wallet.striga.signup.ui

import timber.log.Timber
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpFirstStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor
) :
    BasePresenter<StrigaSignUpFirstStepContract.View>(dispatchers.ui),
    StrigaSignUpFirstStepContract.Presenter {

    private val signupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private val signupDataFlow: MutableSharedFlow<Pair<String, StrigaSignupDataType>> = MutableSharedFlow()

    init {
        signupDataFlow
            .debounce(150L)
            .onEach { (newValue, type) ->
                interactor.updateSignupData(signupData[type]!!)
                Timber.d("Update field $type with value $newValue")
            }
            .launchIn(this)
    }

    override fun attach(view: StrigaSignUpFirstStepContract.View) {
        super.attach(view)
        launch {
            val data = interactor.getSignupData()
            data.forEach {
                signupData[it.type] = it
                view.updateSignupField(it.value ?: "", it.type)
            }

            val country = Country(name = "Turkey", flagEmoji = "", code = "TR") //interactor.getSelectedCountry()
            println(country)
            Timber.d("Country: $country")
            val maskForFormatter = "+" + interactor.findPhoneMaskByCountry(country)
            view.setPhoneMask(maskForFormatter)
        }
    }

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) {
        signupData[type] = StrigaSignupData(type, newValue)
        launch {
            signupDataFlow.emit(newValue to type)
        }
        // enabling button if something changed
        view?.setButtonIsEnabled(true)
    }

    override fun onSubmit() {
        view?.clearErrors()

        val (isValid, states) = interactor.validateFirstStep(signupData)

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
}
