package org.p2p.wallet.auth.ui.reserveusername

import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString
import timber.log.Timber
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnboardingReserveUsernamePresenter(
    private val usernameValidator: UsernameValidator,
    private val usernameInteractor: UsernameInteractor,
) : BasePresenter<OnboardingReserveUsernameContract.View>(),
    OnboardingReserveUsernameContract.Presenter {

    private var currentUsernameEntered = emptyString()

    private var checkUsernameJob: Job? = null

    override fun attach(view: OnboardingReserveUsernameContract.View) {
        super.attach(view)
        view.showUsernameInvalid()
    }

    override fun onUsernameInputChanged(newUsername: String) {
        this.currentUsernameEntered = newUsername
        if (!usernameValidator.isUsernameValid(currentUsernameEntered)) {
            view?.showUsernameInvalid()
            return
        }

        checkUsernameJob = launch {
            try {
                // We should check the availability of the latest entered value by the user
                // therefore we cancel old request if new value is entered and waiting for the latest response only
                delay(500)
                view?.showUsernameIsChecking()
                val isUsernameTaken = usernameInteractor.isUsernameTaken(newUsername)
                if (isUsernameTaken) {
                    view?.showUsernameNotAvailable()
                } else {
                    view?.showUsernameAvailable()
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while checking username: $newUsername")
            }
        }
    }

    override fun onCreateUsernameClicked() {
        launch {
            try {
                view?.showUsernameIsChecking()
                usernameInteractor.registerUsername(currentUsernameEntered)
                view?.close()
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while creating username: $currentUsernameEntered")
                view?.showCreateUsernameFailed()
            }
        }
    }
}
