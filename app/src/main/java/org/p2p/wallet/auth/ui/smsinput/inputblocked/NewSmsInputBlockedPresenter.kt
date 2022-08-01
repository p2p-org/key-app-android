package org.p2p.wallet.auth.ui.smsinput.inputblocked

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
import kotlinx.coroutines.flow.onEach

private const val TIMER_VALUE_FORMAT = "mm:ss"

class NewSmsInputBlockedPresenter :
    BasePresenter<NewAuthSmsInputBlockedContract.View>(),
    NewAuthSmsInputBlockedContract.Presenter {

    override fun attach(view: NewAuthSmsInputBlockedContract.View) {
        super.attach(view)

        createTimerFlow()
            .onEach { this.view?.renderTimerBeforeUnblockValue(createFormattedTimerValue(it)) }
            .launchIn(this)
    }

    private fun createTimerFlow(): Flow<Long> {
        return (5.minutes.inWholeSeconds downTo 0L)
            .asSequence()
            .asFlow()
            .onEach { delay(1.seconds.inWholeMilliseconds) }
    }

    private fun createFormattedTimerValue(secondsLeft: Long): String {
        val formatter = SimpleDateFormat(TIMER_VALUE_FORMAT, Locale.getDefault())
        return formatter.format(Date(secondsLeft.seconds.inWholeMilliseconds))
    }
}
