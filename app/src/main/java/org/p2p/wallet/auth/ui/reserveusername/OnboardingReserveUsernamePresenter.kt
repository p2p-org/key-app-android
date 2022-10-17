package org.p2p.wallet.auth.ui.reserveusername

import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString

class OnboardingReserveUsernamePresenter(
    private val usernameValidator: UsernameValidator,
    private val usernameInteractor: UsernameInteractor
) : BasePresenter<OnboardingReserveUsernameContract.View>(),
    OnboardingReserveUsernameContract.Presenter {

    private var currentUsernameEntered = emptyString()

    override fun attach(view: OnboardingReserveUsernameContract.View) {
        super.attach(view)
        view.showUsernameInvalid()
    }

    override fun onUsernameInputChanged(newUsername: String) {
        this.currentUsernameEntered = newUsername
        if (!usernameValidator.isUsernameValid(currentUsernameEntered)) {
            view?.showUsernameInvalid()
        }
    }

    override fun onCreateUsernameClicked() {
    }
}
