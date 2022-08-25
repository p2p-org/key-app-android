package org.p2p.wallet.auth.ui.generalerror.timer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val TIMER_VALUE_FORMAT = "mm:ss"
private const val START_TIMER_VALUE_MIN = 5

class OnboardingGeneralErrorTimerPresenter(
    private val error: GeneralErrorTimerScreenError
) : BasePresenter<OnboardingGeneralErrorTimerContract.View>(),
    OnboardingGeneralErrorTimerContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorTimerContract.View) {
        super.attach(view)

        createTimerFlow()
            .onEach(::onTimerValueChanged)
            .onCompletion { this@OnboardingGeneralErrorTimerPresenter.view?.navigateToPhoneNumberEnter() }
            .launchIn(this)
    }

    private fun onTimerValueChanged(timerValue: Long) {
        val subTitleRes = when (error) {
            GeneralErrorTimerScreenError.BLOCK_PHONE_NUMBER_ENTER -> {
                R.string.onboarding_general_error_timer_enter_phone_subtitle
            }
            GeneralErrorTimerScreenError.BLOCK_SMS_INPUT -> {
                R.string.onboarding_general_error_timer_sms_input_subtitle
            }
        }
        val formattedTimerValue = createFormattedTimerValue(secondsLeft = timerValue)

        view?.updateSubtitle(
            subTitleRes = subTitleRes,
            formattedTimeLeft = formattedTimerValue
        )
    }

    private fun createTimerFlow(): Flow<Long> {
        return (START_TIMER_VALUE_MIN.minutes.inWholeSeconds downTo 0L)
            .asSequence()
            .asFlow()
            .onEach { delay(1.seconds.inWholeMilliseconds) }
    }

    private fun createFormattedTimerValue(secondsLeft: Long): String {
        val formatter = SimpleDateFormat(TIMER_VALUE_FORMAT, Locale.getDefault())
        return formatter.format(Date(secondsLeft.seconds.inWholeMilliseconds))
    }
}
