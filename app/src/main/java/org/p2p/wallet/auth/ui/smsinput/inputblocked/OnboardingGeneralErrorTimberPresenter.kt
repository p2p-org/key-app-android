package org.p2p.wallet.auth.ui.smsinput.inputblocked

import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

private const val TIMER_VALUE_FORMAT = "mm:ss"
private const val START_TIMER_VALUE_MIN = 5

class OnboardingGeneralErrorTimberPresenter :
    BasePresenter<OnboardingGeneralErrorContract.View>(),
    OnboardingGeneralErrorContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorContract.View) {
        super.attach(view)

        createTimerFlow()
            .onEach { this.view?.renderTimerBeforeUnblock(createFormattedTimerValue(it)) }
            .onCompletion { this@OnboardingGeneralErrorTimberPresenter.view?.navigateToStartingScreen() }
            .launchIn(this)
    }

    override fun setSourceScreen(sourceScreen: OnboardingGeneralErrorContract.View.SourceScreen) {
        when (sourceScreen) {
            OnboardingGeneralErrorContract.View.SourceScreen.PHONE_NUMBER_ENTER -> {
                view?.setTitleAndSubtitle(
                    titleRes = R.string.onboarding_sms_input_phone_number_title,
                    subTitleRes = R.string.onboarding_general_error_timer_enter_phone_subtitle
                )
            }
            OnboardingGeneralErrorContract.View.SourceScreen.SMS_INPUT -> {
                view?.setTitleAndSubtitle(
                    titleRes = R.string.onboarding_sms_input_phone_number_title,
                    subTitleRes = R.string.onboarding_general_error_timer_sms_input_subtitle
                )
            }
        }
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
