package org.p2p.wallet.striga.signup.ui

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpSecondStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor,
) :
    BasePresenter<StrigaSignUpSecondStepContract.View>(dispatchers.ui),
    StrigaSignUpSecondStepContract.Presenter {

    override fun attach(view: StrigaSignUpSecondStepContract.View) {
        super.attach(view)
        launch {
            initialLoadSignupData()
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

        val (isValid, states) = interactor.validateSecondStep()

        if (isValid) {
            view?.navigateNext()
        } else {
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
}
