package org.p2p.wallet.auth.ui.username

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.analytics.OnBoardingAnalytics
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

class ReserveUsernamePresenter(
    private val usernameInteractor: UsernameInteractor,
    private val analytics: OnBoardingAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<ReserveUsernameContract.View>(),
    ReserveUsernameContract.Presenter {

    private var checkUsernameJob: Job? = null
    private var lastUsername: String = emptyString()

    override fun checkUsername(username: String) {
        lastUsername = username
        checkUsernameJob?.cancel()
        if (username.isEmpty()) {
            view?.showIdleState()
            return
        }

        checkUsernameJob = launch {
            try {
                /*
                * We should check the availability of the latest entered value by the user
                * therefore we cancel old request if new value is entered and waiting for the latest response only
                * */
                delay(300)
                view?.showUsernameLoading(true)
                usernameInteractor.checkUsername(username)
                view?.showUnavailableName(username)
            } catch (e: CancellationException) {
                Timber.w(e, "Cancelled request for checking username: $username")
            } catch (e: Throwable) {
                view?.showAvailableName(username)
                Timber.e(e, "Error occurred while checking username: $username")
            }
        }
    }

    override fun checkCaptcha() {
        launch {
            try {
                val params = usernameInteractor.checkCaptcha()
                view?.showCaptcha(params)
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while checking captcha")
                view?.showCaptchaFailed()
                view?.showErrorMessage(e)
            }
        }
    }

    override fun registerUsername(username: String, result: String) {
        view?.showLoading(true)
        launch {
            try {
                usernameInteractor.registerUsername(username, result)
                analytics.logUsernameReserved()
                view?.showSuccess()
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred while registering username")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onSkipClicked() {
        analytics.logUsernameSkipped(OnBoardingAnalytics.UsernameField.getValueOf(lastUsername))
        view?.finishNavigation()
    }

    override fun save() {
        analytics.logUsernameSaved(analyticsInteractor.getPreviousScreenName())
        view?.showCustomFlow()
    }
}
