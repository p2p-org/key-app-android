package org.p2p.wallet.striga.signup.ui

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpSecondStepPresenter(
    private val interactor: StrigaSignupInteractor,
) :
    BasePresenter<StrigaSignUpSecondStepContract.View>(),
    StrigaSignUpSecondStepContract.Presenter {

    private val signupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) {
        signupData[type] = StrigaSignupData(type, newValue)
        // enabling button if something changed
        view?.setButtonIsEnabled(true)
    }

    override fun onSubmit() {
        view?.clearErrors()

        val (isValid, states) = interactor.validateSecondStep(signupData)

        if(isValid) {
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
}
