package org.p2p.wallet.auth.ui.generalerror.timer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.FileInteractor
import org.p2p.wallet.smsinput.SmsInputTimer
import org.p2p.wallet.common.mvp.BasePresenter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

private const val TIMER_VALUE_FORMAT = "mm:ss"

class OnboardingGeneralErrorTimerPresenter(
    private val timerLeftTime: Long,
    private val smsInputTimer: SmsInputTimer,
    private val fileInteractor: FileInteractor
) : BasePresenter<OnboardingGeneralErrorTimerContract.View>(),
    OnboardingGeneralErrorTimerContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorTimerContract.View) {
        super.attach(view)
        // Call this method, to fill emptiness in subtitle, before timer will start count down
        onTimerValueChanged(timerLeftTime.seconds.inWholeSeconds)

        createTimerFlow()
            .onEach(::onTimerValueChanged)
            .onCompletion {
                smsInputTimer.resetSmsCount()
                this@OnboardingGeneralErrorTimerPresenter.view?.navigateToStartingScreen()
            }
            .launchIn(this)
    }

    private fun onTimerValueChanged(timerValue: Long) {
        val formattedTimerValue = createFormattedTimerValue(secondsLeft = timerValue)

        view?.updateText(
            titleRes = R.string.onboarding_general_error_timer_title,
            subTitleRes = R.string.onboarding_general_error_too_many_wrong_attempts,
            formattedTimeLeft = formattedTimerValue
        )
    }

    private fun createTimerFlow(): Flow<Long> {
        return (timerLeftTime.seconds.inWholeSeconds - 1 downTo 0L)
            .asSequence()
            .asFlow()
            .onEach { delay(1.seconds.inWholeMilliseconds) }
    }

    private fun createFormattedTimerValue(secondsLeft: Long): String {
        val formatter = SimpleDateFormat(TIMER_VALUE_FORMAT, Locale.getDefault())
        return formatter.format(Date(secondsLeft.seconds.inWholeMilliseconds))
    }

    override fun onTermsClick() {
        val file = fileInteractor.getTermsOfUseFile()
        view?.showFile(file)
    }

    override fun onPolicyClick() {
        val file = fileInteractor.getPrivacyPolicyFile()
        view?.showFile(file)
    }
}
