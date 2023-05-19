package org.p2p.wallet.auth.ui.reserveusername

import timber.log.Timber
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.username.repository.model.UsernameServiceError
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString

class ReserveUsernamePresenter(
    private val usernameValidator: UsernameValidator,
    private val usernameInteractor: UsernameInteractor,
    private val alertLogger: AlarmErrorsLogger
) : BasePresenter<ReserveUsernameContract.View>(),
    ReserveUsernameContract.Presenter {

    private var currentUsernameEntered = emptyString()

    private var checkUsernameJob: Job? = null
    private var reserveUsernameJob: Job? = null

    override fun attach(view: ReserveUsernameContract.View) {
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
            } catch (invalidUsername: UsernameServiceError.InvalidUsername) {
                Timber.e(invalidUsername, "Invalid username returned: $newUsername")
                view?.showUsernameNotAvailable()
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while checking username: $newUsername")
                logUsernameError(currentUsernameEntered, e)
            }
        }
    }

    override fun onCreateUsernameClicked() {
        reserveUsernameJob?.cancel()
        reserveUsernameJob = launch {
            try {
                view?.showUsernameIsChecking()
                usernameInteractor.registerUsername(currentUsernameEntered)
                view?.close(isUsernameCreated = true)
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while creating username: $currentUsernameEntered")
                logUsernameError(currentUsernameEntered, e)
                view?.showUsernameNotAvailable()
                view?.showCreateUsernameFailed()
            }
        }
    }

    override fun detach() {
        checkUsernameJob?.cancel()
        reserveUsernameJob?.cancel()
        super.detach()
    }

    private fun logUsernameError(username: String, error: Throwable) {
        alertLogger.triggerUsernameAlarm(
            username = username,
            error = error
        )
    }
}
